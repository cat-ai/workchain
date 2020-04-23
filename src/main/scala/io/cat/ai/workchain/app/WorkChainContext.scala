package io.cat.ai.workchain.app

import io.cat.ai.workchain.WorkChainEnv
import io.cat.ai.workchain.app.executor.AbstractDagJobExecutor
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.executor.ExecutionMode
import io.cat.ai.workchain.core.graph.directed.DirectedAcyclicGraph
import io.cat.ai.workchain.core.graph.{DAG, Edge}

trait WorkChainContext {

  def executionMode: ExecutionMode

  def executorGroup: AbstractDagJobExecutor

  def jobGraph: DAG[Job, Edge[Job]]

  def startExecution(): Unit
}

class DefaultTaskContext(override val executionMode: ExecutionMode,
                         override val executorGroup: AbstractDagJobExecutor) extends WorkChainContext {

  private val jobDAG: DirectedAcyclicGraph[Job, Edge[Job]] = WorkChainEnv.createDAG

  override def jobGraph: DAG[Job, Edge[Job]] = jobDAG

  override def startExecution(): Unit = executorGroup.submit(jobGraph)
}

object WorkChainContext {

  def createDefaultContext(executionMode: ExecutionMode, executorGroup: AbstractDagJobExecutor): WorkChainContext = new DefaultTaskContext(executionMode, executorGroup)

  def apply(exMode: ExecutionMode,
            exGroup: AbstractDagJobExecutor,
            jobDag: DAG[Job, Edge[Job]]): WorkChainContext = new WorkChainContext {
    override def executionMode: ExecutionMode = exMode

    override def executorGroup: AbstractDagJobExecutor = exGroup

    override def jobGraph: DAG[Job, Edge[Job]] = jobDag

    override def startExecution(): Unit = executorGroup.submit(jobGraph)
  }
}