package io.cat.ai.workchain.app.scheduler.manager.jobset

import io.cat.ai.workchain.common.{Listener, State}
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.scheduler.manager.JobSetManager
import io.cat.ai.workchain.util.AbstractHSetMultiMap
import io.cat.ai.workchain.util.MultiMap._

protected[manager] class SynchronizedJobSetManager(val schedulerAllJobs: MutSet[Job],
                                                   val schedulerChainedJobs: AbstractHSetMultiMap[Job, Job],
                                                   val schedulerFailedJobs: MutSet[(Job, Throwable)])
                                                  (override implicit val listener: Listener[String, Job]) extends JobSetManager[Job] {

  private final val monitor: AnyRef = new AnyRef

  override def jobState(schedulerId: String, job: Job): Option[State] =
    monitor synchronized {
      ???
    }

  override def takeJob: Option[Job] = synchronized {
    ???
  }

  override def markDone(schedulerId: String, job: Job): Unit =
    monitor synchronized {
      ???
    }

  override def markFail(schedulerId: String, job: Job, throwable: Throwable): Unit =
    monitor synchronized {
      ???
    }

  override def hasUncompletedJobs: Boolean = ???

  override def markRetry(schedulerId: String, job: Job): Unit = monitor synchronized {
    ???
  }

  override def removeJob(schedulerId: String, job: Job): Option[Job] = monitor synchronized {
    ???
  }

  override def markRecover(schedulerId: String, job: Job, recover: Runnable): Unit = monitor synchronized {
    ???
  }
}
