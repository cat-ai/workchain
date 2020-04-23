package io.cat.ai.workchain.concurrent

import scala.concurrent.duration.Duration

trait ManageableExecutorWithDelayGroup[A] extends AbstractExecutorGroup[A] with ManageableExecutorGroup {

  def executeWithDelay(command: Runnable, duration: Duration): Int

  override def execute(command: Runnable): Unit

  override def submit(a: A): Unit

  override def shutdown(): Unit
}