package io.cat.ai.workchain.core.executor

import io.cat.ai.workchain.concurrent.MultithreadedManageableExecutorGroup
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.graph.{DAG, Edge}

trait MultithreadedManageableDagExecutorGroup extends DagExecutorGroup with MultithreadedManageableExecutorGroup[DAG[Job, Edge[Job]]] {

  override def execute(command: Runnable): Unit

  override def hasJobs: Boolean

  override def inProcess: Seq[Runnable]

  override def poolSize: Int

  override def shutdown(): Unit

  override def submit(a: DAG[Job, Edge[Job]]): Unit
}
