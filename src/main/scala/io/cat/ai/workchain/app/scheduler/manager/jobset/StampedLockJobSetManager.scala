package io.cat.ai.workchain.app.scheduler.manager.jobset

import java.util.concurrent.locks.{Lock, StampedLock}

import io.cat.ai.workchain.common._
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.scheduler.manager.JobSetManager
import io.cat.ai.workchain.util.AbstractHSetMultiMap
import io.cat.ai.workchain.util.MultiMap._

import scala.annotation.tailrec

protected[manager] class StampedLockJobSetManager(val schedulerAllJobs: MutSet[Job],
                                                  val schedulerChainedJobs: AbstractHSetMultiMap[Job, Job],
                                                  val schedulerFailedJobs: MutSet[(Job, Throwable)],
                                                  val schedulerJobState: MutMap[Job, State])
                                                 (override implicit val listener: Listener[String, Job]) extends JobSetManager[Job] {

  private val stampedLock: StampedLock = new StampedLock
  private val readLock: Lock = stampedLock.asReadLock
  private val writeLock: Lock = stampedLock.asWriteLock

  override def jobState(schedulerId: String, job: Job): Option[State] = schedulerJobState.get(job)

  override def takeJob: Option[Job] = {

    def takeJobOpt(iterator: Iterator[Job]): Option[Job] = {

      @tailrec
      def innerLoop(condition: Boolean, acc: Option[Job] = None): Option[Job] = {
        if (condition) {
          val job = iterator.next

          (if (schedulerChainedJobs.contains(job))
            schedulerChainedJobs.get(job)
          else
            None) match {
            case Some(chainedJobs) if chainedJobs.isEmpty => Some(job)
            case Some(_) => innerLoop(iterator.hasNext, acc)
            case None => Some(job)
          }
        }
        else acc
      }
      innerLoop(iterator.hasNext)
    }

    writeLock.lock()
    try
      for {
        job <- takeJobOpt(schedulerAllJobs.iterator)
        _   =  schedulerAllJobs -= job
      } yield job
    finally
      writeLock.unlock()
  }

  override def markDone(schedulerId: String, job: Job): Unit = {
    writeLock.lock()
    try {
      schedulerChainedJobs.removeValue(job)
      schedulerJobState += job -> Done
      listener.onComplete(schedulerId, job)
    } finally
      writeLock.unlock()
  }

  override def markFail(schedulerId: String, job: Job, throwable: Throwable): Unit = {
    writeLock.lock()
    try {
      schedulerFailedJobs += job -> throwable
      schedulerJobState += job -> Fail
      listener.onFailure(schedulerId, job, throwable)
    } finally
      writeLock.unlock()
  }

  override def hasUncompletedJobs: Boolean = {
    readLock.lock()
    try
      schedulerAllJobs.nonEmpty
    finally
      readLock.unlock()
  }

  override def markRetry(schedulerId: String, job: Job): Unit = {
    writeLock.lock()
    try {
      schedulerJobState += job -> Retry
      listener.onRetry(schedulerId, job)
    } finally
      writeLock.unlock()
  }

  override def markRecover(schedulerId: String, job: Job, recover: Runnable): Unit = {
    writeLock.lock()
    try {
      schedulerJobState += job -> Recover
      listener.onRecover(schedulerId, job, recover)
    } finally
      writeLock.unlock()
  }

  override def removeJob(schedulerId: String, job: Job): Option[Job] = {
    writeLock.lock()
    try
      if (schedulerAllJobs remove job)
        Some(job)
      else None
    finally writeLock.unlock()
  }
}