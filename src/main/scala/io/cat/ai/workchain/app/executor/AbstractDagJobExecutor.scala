package io.cat.ai.workchain.app.executor

import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

import io.cat.ai.workchain.common.FiniteConditionalLoop
import io.cat.ai.workchain.concurrent.job.{AbstractRetryableJob, Job, NonRetryableJob, NonRetryableJobWithRecover, RecoverableJob, RetryableJob, RetryableJobWithRecover}
import io.cat.ai.workchain.core.scheduler.manager.{DagSchedulerManager, JobSetManager}

import scala.collection.mutable

abstract class AbstractDagJobExecutor extends DagJobExecutor[String] with FiniteConditionalLoop[String, JobSetManager[Job], Unit] {

  protected val running: AtomicBoolean

  def randUUID: String = s"${UUID.randomUUID}"

  override val managerCache: mutable.Map[String, DagSchedulerManager[Job, Throwable, mutable.Set]] = new mutable.HashMap

  override def execute(uuid: String, command: Runnable): Unit = {

    def executeRecoverableJob(schedulerManager: DagSchedulerManager[Job, Throwable, mutable.Set], recoverableJob: RecoverableJob): Unit =
      try {
        schedulerManager.jobSetManager.markRecover(uuid, recoverableJob, recoverableJob.recover())
        recoverableJob.performRecover()
        schedulerManager.jobSetManager.markDone(uuid, recoverableJob)
      } catch {
        case exc: Throwable => schedulerManager.jobSetManager.markFail(uuid, recoverableJob, exc)
      }

    def executeRetryableJob(schedulerManager: DagSchedulerManager[Job, Throwable, mutable.Set], retryableJob: AbstractRetryableJob): Unit = {
      def retryAfterFail(): Unit =
        try {
          schedulerManager.jobSetManager.markRetry(uuid, retryableJob)
          retryableJob.retry()
          schedulerManager.jobSetManager.markDone(uuid, retryableJob)
        } catch {
          case innerEx: Throwable =>
            schedulerManager.jobSetManager.markFail(uuid, retryableJob, innerEx)
            execute0(schedulerManager, retryableJob, Some(innerEx))
        }

      try {
        retryableJob.run()
        schedulerManager.jobSetManager.markDone(uuid, retryableJob)
      } catch {
        case ex: Throwable =>
          schedulerManager.jobSetManager.markFail(uuid, retryableJob, ex)
          retryAfterFail()
      }
    }

    def execute0(schedulerManager: DagSchedulerManager[Job, Throwable, mutable.Set], _job: Job, excOpt: Option[Throwable] = None): Unit = _job match {
      case nonRetryableJob: NonRetryableJob =>
        try {
          nonRetryableJob.run()
          schedulerManager.jobSetManager.markDone(uuid, nonRetryableJob)
        } catch {
          case exc: Throwable => schedulerManager.jobSetManager.markFail(uuid, nonRetryableJob, exc)
        }

      case nonRetryableJobWithRecover: NonRetryableJobWithRecover =>
        try {
          nonRetryableJobWithRecover.run()
          schedulerManager.jobSetManager.markDone(uuid, nonRetryableJobWithRecover)
        } catch {
          case exc: Throwable =>
            schedulerManager.jobSetManager.markFail(uuid, nonRetryableJobWithRecover, exc)
            executeRecoverableJob(schedulerManager, nonRetryableJobWithRecover)
        }

      case retryableJob: RetryableJob if retryableJob.isRetried => for (exception <- excOpt) schedulerManager.jobSetManager.markFail(uuid, retryableJob, exception)

      case retryableJob: RetryableJob => executeRetryableJob(schedulerManager, retryableJob)

      case retryableJobWithRecover: RetryableJobWithRecover if retryableJobWithRecover.isRetried => executeRecoverableJob(schedulerManager, retryableJobWithRecover)

      case retryableJobWithRecover: RetryableJobWithRecover => executeRetryableJob(schedulerManager, retryableJobWithRecover)
    }

    managerCache.get(uuid) match {
      case Some(schedulerManager) => execute0(schedulerManager, command.asInstanceOf[Job])
      case _ => ()
    }
  }
}