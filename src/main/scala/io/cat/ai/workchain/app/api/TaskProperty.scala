package io.cat.ai.workchain.app.api

trait TaskProperty

case object Retryable extends TaskProperty
case object RetryableRecoverable extends TaskProperty
case object NonRetryable extends TaskProperty
case object NonRetryableRecoverable extends TaskProperty