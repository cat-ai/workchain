package io.cat.ai.workchain

import io.cat.ai.workchain.app.{WorkChain, WorkChainSession}
import io.cat.ai.workchain.app.executor.AbstractDagJobExecutor
import io.cat.ai.workchain.common.{Done, Fail, Listener, Recover, Retry}
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.graph.directed.DirectedAcyclicGraph
import io.cat.ai.workchain.core.graph.{Edge, dag}
import io.cat.ai.workchain.core.scheduler.manager.DagSchedulerManager

import scala.collection.mutable

object WorkChainEnv {

  private lazy val workChainAppInstance: WorkChain = WorkChain()

  private val apps: mutable.Map[String, WorkChain] = new mutable.HashMap[String, WorkChain]
  private val sessionExecutorsMap: mutable.Map[String, AbstractDagJobExecutor] = new mutable.HashMap[String, AbstractDagJobExecutor]

  private def registerApp(workChain: WorkChain): Unit = {
    apps += workChain.id -> workChain
    sessionExecutorsMap += workChain.id -> workChain.executorGroup
  }

  def createDAG: DirectedAcyclicGraph[Job, Edge[Job]] = dag.empty[Job, Edge[Job]]

  def sessionExecutors: mutable.Map[String, AbstractDagJobExecutor] = sessionExecutorsMap

  def application: WorkChain = {
    val app = workChainAppInstance
    registerApp(app)
    app
  }

  def sessions: mutable.Map[String, WorkChainSession] = application.sessions

  def createListener(managerCache: mutable.Map[String, DagSchedulerManager[Job, Throwable, mutable.Set]]): Listener[String, Job] = new Listener[String, Job] {

    override def onComplete(id: String, a: Job): Unit =
      managerCache.get(id) match {
        case Some(schedulerManager) =>
          schedulerManager.jobState += a -> Done
          println(s"$a job done")

        case _ => ()
      }

    override def onFailure(id: String, a: Job, throwable: Throwable): Unit =
      managerCache.get(id) match {
        case Some(schedulerManager) =>
          schedulerManager.jobState += a -> Fail
          println(s"$a job failed. Cause: ${throwable.getMessage}")

        case _ => ()
      }

    override def onRetry(id: String, a: Job): Unit =
      managerCache.get(id) match {
        case Some(schedulerManager) =>
          schedulerManager.jobState += a -> Retry
          println(s"Retrying $a")

        case _ => ()
      }

    override def onRecover(id: String, a: Job, recover: Runnable): Unit = {
      managerCache.get(id) match {
        case Some(schedulerManager) =>
          schedulerManager.jobState += a -> Recover
          println(s"Recovering $a with $recover")

        case _ => ()
      }
    }

    override def onError(id: String, a: Job, throwable: Throwable): Unit = {
      // add later fault-tolerance functionality
    }
  }

  def stop(): Unit = for(sessionExecutor <- sessionExecutors.values) sessionExecutor.shutdown()
}
