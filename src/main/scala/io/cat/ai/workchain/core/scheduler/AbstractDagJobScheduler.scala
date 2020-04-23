package io.cat.ai.workchain.core.scheduler

import io.cat.ai.workchain.common.{Init, Listener}
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.graph._
import io.cat.ai.workchain.core.scheduler.manager.DagSchedulerManager

import scala.collection.mutable.{Set => MutSet}

abstract class AbstractDagJobScheduler extends DagScheduler[Job, Edge[Job], DagSchedulerManager[Job, Throwable, MutSet]] {

  protected def split(dagSchedulerManager: DagSchedulerManager[Job, Throwable, MutSet], job: Job, predecessors: Seq[Job]): Unit = {
    dagSchedulerManager.allJobs += job
    dagSchedulerManager.allJobs ++= predecessors
    dagSchedulerManager.jobState += job -> Init

    for (predecessor <- predecessors)
      dagSchedulerManager.chainedJobs putValue(job, predecessor)
  }

  override def schedule(dag: DAG[Job, Edge[Job]]) (implicit listener: Listener[String, Job]): DagSchedulerManager[Job, Throwable, MutSet]
}