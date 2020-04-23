package io.cat.ai.workchain.concurrent.job

import io.cat.ai.workchain.common.Recoverable

trait RecoverableJob extends Job with Recoverable[Runnable] { self =>

  override val recover: () => Runnable

  final def performRecover(): Unit = {
    val recoverJob = recover()
    recoverJob.run()
  }
}