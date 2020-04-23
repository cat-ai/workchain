package io.cat.ai.workchain.core.executor

sealed trait ExecutionMode {
  def nThreads: Int
}

case object SingleThreaded extends ExecutionMode {
  override def nThreads: Int = 1
}

final case class MultiThreaded(nThreads: Int) extends ExecutionMode

final case class ParallelMultiThreaded(nThreads: Int, parallelism: Int = 4) extends ExecutionMode