package io.cat.ai.workchain.concurrent.job

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import io.cat.ai.workchain.common.Retryable
import io.cat.ai.workchain.common.AtomicOps.BooleanOps._

trait Job extends Runnable {

  def delegate: Runnable

  def uuid: String
}

final case class RetryableJob(override val delegate: Runnable,
                              override val uuid: String) extends AbstractRetryableJob { self =>

  override def retry(): Unit = {
    retried := true
    delegate.run()
  }

  override def toString: String = s"RetryableJob[$uuid]"
}

final case class RetryableJobWithRecover(override val delegate: Runnable,
                                         override val uuid: String,
                                         override val recover: () => Runnable) extends AbstractRetryableJob with RecoverableJob { self =>

  override def retry(): Unit = {
    retried := true
    delegate.run()
  }

  override def toString: String = s"RetryableJobWithRecover[$uuid]"
}

final case class NonRetryableJob(override val delegate: Runnable,
                                 override val uuid: String) extends Job { self =>

  override def run(): Unit = delegate.run()

  override def toString: String = s"NonRetryableJob[$uuid]"

}

final case class NonRetryableJobWithRecover(override val delegate: Runnable,
                                            override val uuid: String,
                                            override val recover: () => Runnable) extends RecoverableJob { self =>

  override def run(): Unit = delegate.run()

  override def toString: String = s"NonRetryableJobWithRecover[$uuid]"
}

object Job {

  import java.util.UUID

  private val atomicLong: AtomicLong = new AtomicLong(0)

  def randUUID: String = s"${UUID.randomUUID}-${atomicLong.incrementAndGet}"

  def apply(task: Runnable, uuid: String = randUUID): Job = default(task, uuid)

  def default(task: Runnable, uuid: String = randUUID): Job = nonRetryable(task, uuid)

  def nonRetryable(task: Runnable, uuid: String = randUUID): Job = NonRetryableJob(task, uuid)

  def nonRetryableWithRecover(task: Runnable, recover: Runnable, uuid: String = randUUID): RecoverableJob = NonRetryableJobWithRecover(task, uuid, () => recover)

  def retryable(task: Runnable, uuid: String = randUUID): Job with Retryable = RetryableJob(task, uuid)

  def retryableWithRecover(task: Runnable, recover: => Runnable, uuid: String = randUUID): RecoverableJob with Retryable = RetryableJobWithRecover(task, uuid, () => recover)
}