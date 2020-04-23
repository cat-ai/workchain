package io.cat.ai.workchain.app.executor

import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

import io.cat.ai.workchain.common.AtomicOps.BooleanOps._
import io.cat.ai.workchain.core.executor.SingleThreadedDagExecutorGroup
import io.cat.ai.workchain.core.scheduler.AbstractDagJobScheduler

final class SingleThreadedDagJobExecutor (override val scheduler: AbstractDagJobScheduler)
                                         (override implicit val workerPool: ExecutorService) extends AbstractDagJobPooledExecutor with SingleThreadedDagExecutorGroup {

  protected override val running: AtomicBoolean = new AtomicBoolean(true)

  override def shutdown(): Unit = {
    workerPool.shutdownNow()
    running := false
  }
}