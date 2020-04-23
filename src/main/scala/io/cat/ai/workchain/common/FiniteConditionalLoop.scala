package io.cat.ai.workchain.common

trait FiniteConditionalLoop[-A, -B, +C] {
  def runRecLoop(a: A, b: B, condition: Boolean): C
}