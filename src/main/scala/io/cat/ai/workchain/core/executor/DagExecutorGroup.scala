package io.cat.ai.workchain.core.executor

import java.util.concurrent.ExecutorService

import io.cat.ai.workchain.concurrent.AbstractExecutorGroup
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.graph.{DAG, Edge}
import io.cat.ai.workchain.core.scheduler.AbstractDagJobScheduler

trait DagExecutorGroup extends AbstractExecutorGroup[DAG[Job, Edge[Job]]] {

  implicit def workerPool: ExecutorService

  override def submit(dag: DAG[Job, Edge[Job]]): Unit

  override def shutdown(): Unit

  def scheduler: AbstractDagJobScheduler
}