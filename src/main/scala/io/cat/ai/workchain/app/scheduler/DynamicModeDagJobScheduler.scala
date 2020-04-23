package io.cat.ai.workchain.app.scheduler

import java.util.concurrent.atomic.AtomicReference

import io.cat.ai.workchain.app.schedulers.managers
import io.cat.ai.workchain.common.Listener
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.graph.{DAG, Edge}
import io.cat.ai.workchain.core.scheduler.AbstractDagJobScheduler
import io.cat.ai.workchain.core.scheduler.manager.{DagSchedulerManager, JobSetManagerMode, LockMode}
import io.cat.ai.workchain.util.MultiMap.MutSet

final class DynamicModeDagJobScheduler(mode: JobSetManagerMode) extends AbstractDagJobScheduler with DynamicModeScheduler { self =>

  private val modeAtomic: AtomicReference[JobSetManagerMode] = new AtomicReference[JobSetManagerMode](mode)

  override def schedule(dag: DAG[Job, Edge[Job]]) (implicit listener: Listener[String, Job]): DagSchedulerManager[Job, Throwable, MutSet] = {
    val dagSchedulerManager: DagSchedulerManager[Job, Throwable, MutSet] = managers.dagSchedulerManager(modeAtomic.get)

    for (vertex <- dag.vertices())
      split(dagSchedulerManager, vertex, dag.predecessors(vertex))

    dagSchedulerManager
  }

  override def applyJobSetManagerMode(mode: JobSetManagerMode): self.type = {
    self.modeAtomic.set(mode)
    self
  }
}
