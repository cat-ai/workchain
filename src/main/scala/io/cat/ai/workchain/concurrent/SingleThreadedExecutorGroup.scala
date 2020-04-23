package io.cat.ai.workchain.concurrent

trait SingleThreadedExecutorGroup[A] extends AbstractExecutorGroup[A] {

  override def submit(a: A): Unit

  override def execute(command: Runnable): Unit

  override def shutdown(): Unit
}