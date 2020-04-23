package io.cat.ai.workchain.core.executor.internal

import java.util.concurrent.atomic.AtomicBoolean

trait FastExecutorServiceWorker extends Runnable {
  def running: AtomicBoolean
}