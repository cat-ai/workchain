package io.cat.ai.workchain.common

trait Recoverable[+A] {
  def recover(): () => A
}