package io.cat.ai.workchain.core.scheduler.manager

import io.cat.ai.workchain.common.{Listener, State}

/**
 * Each DAG object contains a set of tasks that are related by dependencies - the "directed" in directed acyclic graph
*/
trait JobSetManager[Job <: Runnable] {

  def hasUncompletedJobs: Boolean

  def jobState(schedulerId: String, job: Job): Option[State]

  def takeJob: Option[Job]

  def removeJob(schedulerId: String, job: Job): Option[Job]

  def markDone(schedulerId: String, job: Job): Unit

  def markFail(schedulerId: String, job: Job, throwable: Throwable): Unit

  def markRetry(schedulerId: String, job: Job): Unit

  def markRecover(schedulerId: String, job: Job, recover: Runnable): Unit

  implicit def listener: Listener[String, Job]
}