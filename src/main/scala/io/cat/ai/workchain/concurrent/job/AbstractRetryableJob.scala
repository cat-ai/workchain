
package io.cat.ai.workchain.concurrent.job

import java.util.concurrent.atomic.AtomicBoolean

import io.cat.ai.workchain.common.Retryable
import io.cat.ai.workchain.common.AtomicOps.BooleanOps._

abstract class AbstractRetryableJob extends Job with Retryable { self =>

  protected final val retried: AtomicBoolean = new AtomicBoolean(false)

  override def retry(): Unit

  override def isRetried: Boolean = is(retried)

  override def run(): Unit = delegate.run()
}