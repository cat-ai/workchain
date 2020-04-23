package io.cat.ai.workchain.concurrent

import java.util.concurrent.ExecutorService

trait ManageableParallelizableExecutorGroup[A] extends MultithreadedManageableExecutorGroup[A] with ParallelizableExecutorGroup {

  implicit def parallelizerPool: ExecutorService

  def poolSize: Int

  def parallelismLevel: Int

  override def execute(command: Runnable): Unit

  override def submit(a: A): Unit

  override def shutdown(): Unit
}