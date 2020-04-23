package io.cat.ai.workchain.core.scheduler.manager

sealed trait JobSetManagerMode

case object LockMode extends JobSetManagerMode

case object SynchronizedMode extends JobSetManagerMode