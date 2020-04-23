package io.cat.ai.workchain.concurrent

trait ParallelizableExecutorGroup extends ExecutorGroup {

  def parallelismLevel: Int
}