package io.cat.ai.workchain.common

trait Listener[A, B] {

  def onComplete(a: A, b: B): Unit

  def onFailure(a: A, b: B, throwable: Throwable): Unit

  def onRetry(a: A, b: B): Unit

  def onRecover(id: String, a: B, recover: Runnable): Unit

  def onError(id: String, a: B, throwable: Throwable): Unit
}