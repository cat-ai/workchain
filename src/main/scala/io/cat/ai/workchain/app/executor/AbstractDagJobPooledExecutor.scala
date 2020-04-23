package io.cat.ai.workchain.app.executor

import java.util.concurrent.atomic.AtomicBoolean

import io.cat.ai.workchain.WorkChainEnv
import io.cat.ai.workchain.common.AtomicOps.BooleanOps.is
import io.cat.ai.workchain.common.Listener
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.graph.{DAG, Edge}
import io.cat.ai.workchain.core.scheduler.manager.{DagSchedulerManager, JobSetManager}

import scala.annotation.tailrec
import scala.collection.mutable

abstract class AbstractDagJobPooledExecutor extends AbstractDagJobExecutor {

  override protected val running: AtomicBoolean

  @tailrec
  override final def runRecLoop(uuid: String, jobSetManager: JobSetManager[Job], condition: Boolean): Unit =
    if (condition) {
      jobSetManager.takeJob match {
        case Some(job) => execute(uuid, job)
        case None      => ()
      }
      runRecLoop(uuid, jobSetManager, is(running))
    } else ()

  override def submit(dag: DAG[Job, Edge[Job]]): Unit = execute { () =>
    implicit val listener: Listener[String, Job] = WorkChainEnv.createListener(managerCache)
    val dagSchedulerManager: DagSchedulerManager[Job, Throwable, mutable.Set] = scheduler.schedule(dag)

    val uuid = randUUID
    managerCache += uuid -> dagSchedulerManager

    runRecLoop(uuid, dagSchedulerManager.jobSetManager, is(running))
  }

}
