package io.cat.ai.workchain.app.scheduler

import io.cat.ai.workchain.app.schedulers.managers
import io.cat.ai.workchain.common.Listener
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.graph.{DAG, Edge}
import io.cat.ai.workchain.core.scheduler.AbstractDagJobScheduler
import io.cat.ai.workchain.core.scheduler.manager.{DagSchedulerManager, LockMode}

import scala.collection.mutable.{Set => MutSet}

final class DefaultDagJobScheduler extends AbstractDagJobScheduler {

  override def schedule(dag: DAG[Job, Edge[Job]])
                       (implicit listener: Listener[String, Job]): DagSchedulerManager[Job, Throwable, MutSet] = {
    val dagSchedulerManager: DagSchedulerManager[Job, Throwable, MutSet] = managers.dagSchedulerManager(LockMode)

    for (vertex <- dag.vertices())
      split(dagSchedulerManager, vertex, dag.predecessors(vertex))

    dagSchedulerManager
  }
}