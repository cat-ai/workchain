package io.cat.ai.workchain.app.executor

import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

import io.cat.ai.workchain.common.AtomicOps.BooleanOps._
import io.cat.ai.workchain.common.InProcess
import io.cat.ai.workchain.core.executor.MultithreadedManageableDagExecutorGroup
import io.cat.ai.workchain.core.scheduler.AbstractDagJobScheduler

final class MultithreadedDagJobExecutor(override val scheduler: AbstractDagJobScheduler,
                                        override val poolSize: Int)
                                       (implicit val workerPool: ExecutorService) extends AbstractDagJobPooledExecutor with MultithreadedManageableDagExecutorGroup {

  protected override val running: AtomicBoolean = new AtomicBoolean(true)

  override def hasJobs: Boolean = managerCache.values.map(_.jobSetManager).count(_.hasUncompletedJobs) > 0

  override def inProcess: Seq[Runnable] = {
    for ((job, state) <- managerCache.values.flatMap(_.jobState) if state == InProcess)
      yield job
  }.toSeq

  override def shutdown(): Unit = {
    workerPool.shutdownNow()
    running := false
  }

  override def isActive: Boolean = is(running)
}