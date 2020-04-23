package io.cat.ai.workchain.core.executor

import io.cat.ai.workchain.concurrent.SingleThreadedExecutorGroup
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.graph.{DAG, Edge}
import io.cat.ai.workchain.core.scheduler.AbstractDagJobScheduler

trait SingleThreadedDagExecutorGroup extends DagExecutorGroup with SingleThreadedExecutorGroup[DAG[Job, Edge[Job]]] {

  override def execute(command: Runnable): Unit

  override def shutdown(): Unit

  override def submit(a: DAG[Job, Edge[Job]]): Unit

  override def scheduler: AbstractDagJobScheduler
}