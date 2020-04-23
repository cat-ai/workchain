package io.cat.ai.workchain.core.scheduler.manager

import io.cat.ai.workchain.common.{Listener, State}
import io.cat.ai.workchain.util.AbstractHSetMultiMap

import scala.collection.mutable.{Map => MutMap}
import scala.language.higherKinds

trait DagSchedulerManager[Job <: Runnable, Error <: Throwable, C[_]] {

  def allJobs: C[Job]

  def chainedJobs: AbstractHSetMultiMap[Job, Job]

  def jobsWithError: C[(Job, Error)]

  def jobState: MutMap[Job, State]

  def jobSetManager: JobSetManager[Job]
}