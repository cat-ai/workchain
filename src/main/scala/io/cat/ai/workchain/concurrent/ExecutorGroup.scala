package io.cat.ai.workchain.concurrent

import java.util.concurrent.Executor

trait ExecutorGroup extends Executor {

  override def execute(command: Runnable): Unit

  def shutdown(): Unit
}