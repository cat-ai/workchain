package io.cat.ai.workchain.core.executor

import io.cat.ai.workchain.concurrent.ManageableParallelizableExecutorGroup
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.graph.{DAG, Edge}

trait ManageableParallelizableDagExecutorGroup extends DagExecutorGroup with ManageableParallelizableExecutorGroup[DAG[Job, Edge[Job]]] {

  override def execute(command: Runnable): Unit

  override def hasJobs: Boolean

  override def inProcess: Seq[Runnable]

  override def parallelismLevel: Int

  override def poolSize: Int

  override def shutdown(): Unit

  override def submit(a: DAG[Job, Edge[Job]]): Unit
}