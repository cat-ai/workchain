package io.cat.ai.workchain.app.api

sealed trait TaskWrapper {

  def task: Runnable

  def property: TaskProperty
}

final case class TaskWrapperRet(override val task: Runnable) extends TaskWrapper {
  override def property: TaskProperty = Retryable
}

final case class TaskWrapperNRet(override val task: Runnable) extends TaskWrapper {
  override def property: TaskProperty = NonRetryable
}

final case class TaskWrapperNRetRec(override val task: Runnable, recover: Runnable) extends TaskWrapper {
  override def property: TaskProperty = NonRetryableRecoverable
}

final case class TaskWrapperRetRec(override val task: Runnable, recover: Runnable) extends TaskWrapper {
  override def property: TaskProperty = RetryableRecoverable
}

object TaskWrapper {

  def ret(task: Runnable): TaskWrapper = TaskWrapperRet(task)

  def nret(task: Runnable): TaskWrapper = TaskWrapperNRet(task)

  def retrec(task: Runnable,
             recover: Runnable): TaskWrapper = TaskWrapperRetRec(task, recover)

  def nretrec(task: Runnable,
              recover: Runnable): TaskWrapper = TaskWrapperNRetRec(task, recover)
}