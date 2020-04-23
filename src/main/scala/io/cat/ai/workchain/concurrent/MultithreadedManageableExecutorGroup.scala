package io.cat.ai.workchain.concurrent

trait MultithreadedManageableExecutorGroup[A] extends AbstractExecutorGroup[A] with ManageableExecutorGroup {

  def poolSize: Int

  override def execute(command: Runnable): Unit

  override def submit(a: A): Unit

  override def shutdown(): Unit
}