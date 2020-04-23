package io.cat.ai.workchain.app.executor

import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.executor.DagExecutorGroup
import io.cat.ai.workchain.core.scheduler.manager.DagSchedulerManager

import scala.collection.mutable

trait DagJobExecutor[A] extends DagExecutorGroup {

  def managerCache: mutable.Map[String, DagSchedulerManager[Job, Throwable, mutable.Set]]

  def execute(a: A, command: Runnable): Unit

  override def execute(command: Runnable): Unit = workerPool.execute(command)
}