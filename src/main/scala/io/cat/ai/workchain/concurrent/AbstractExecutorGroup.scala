package io.cat.ai.workchain.concurrent

abstract class AbstractExecutorGroup[A] extends ExecutorGroup {

  def submit(a: A): Unit
}