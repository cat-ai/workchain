package io.cat.ai.workchain.common

trait Retryable {

  def retry(): Unit

  def isRetried: Boolean
}