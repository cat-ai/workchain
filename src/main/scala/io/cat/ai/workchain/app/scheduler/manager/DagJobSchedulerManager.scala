package io.cat.ai.workchain.app.scheduler.manager

import io.cat.ai.workchain.app.scheduler.manager.jobset.{StampedLockJobSetManager, SynchronizedJobSetManager}
import io.cat.ai.workchain.common.{Listener, State}
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.scheduler.manager.{DagSchedulerManager, JobSetManager, JobSetManagerMode, LockMode, SynchronizedMode}
import io.cat.ai.workchain.util.{AbstractHSetMultiMap, HSetMultiMap}
import io.cat.ai.workchain.util.MultiMap._

final class DagJobSchedulerManager(jobSetManagerMode: JobSetManagerMode)
                                  (implicit listener: Listener[String, Job]) extends DagSchedulerManager[Job, Throwable, MutSet] {

  private[this] val allJobsSet: MutSet[Job] = new MutHSet[Job]
  private[this] val chainedJobsMultiMap: AbstractHSetMultiMap[Job, Job] = HSetMultiMap.empty[Job, Job]
  private[this] val jobsWithErrorSet: MutSet[(Job, Throwable)] = new MutHSet[(Job, Throwable)]
  private[this] val jobStateMap: MutMap[Job, State] = new MutHMap[Job, State]

  override def allJobs: MutSet[Job] = allJobsSet

  override def chainedJobs: AbstractHSetMultiMap[Job, Job] = chainedJobsMultiMap

  override def jobsWithError: MutSet[(Job, Throwable)] = jobsWithErrorSet

  override def jobSetManager: JobSetManager[Job] = jobSetManagerMode match {
    case LockMode         => new StampedLockJobSetManager(allJobsSet, chainedJobsMultiMap, jobsWithErrorSet, jobStateMap)
    case SynchronizedMode => new SynchronizedJobSetManager(allJobsSet, chainedJobsMultiMap, jobsWithErrorSet)
  }

  override def jobState: MutMap[Job, State] = jobStateMap
}
