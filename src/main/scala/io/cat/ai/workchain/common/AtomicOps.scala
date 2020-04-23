package io.cat.ai.workchain.common

import java.util.concurrent.atomic.AtomicBoolean

object AtomicOps {

  object BooleanOps {

    implicit class AtomicBooleanOp(atomicBool: AtomicBoolean) {
      def := (newValue: Boolean): Unit = atomicBool.set(newValue)
    }

    def is(atomicBool: AtomicBoolean): Boolean = atomicBool.get()

    def not(atomicBool: AtomicBoolean): Boolean = ! is(atomicBool)
  }

}
