package io.cat.ai.workchain.concurrent

trait ManageableExecutorGroup extends ExecutorGroup {

  def isActive: Boolean

  def hasJobs: Boolean

  def inProcess: Seq[Runnable]
}