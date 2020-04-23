package io.cat.ai.workchain.app.executor

import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

import io.cat.ai.workchain.WorkChainEnv
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.executor.ManageableParallelizableDagExecutorGroup
import io.cat.ai.workchain.core.graph.{DAG, Edge}
import io.cat.ai.workchain.core.scheduler.AbstractDagJobScheduler
import io.cat.ai.workchain.common.AtomicOps.BooleanOps._
import io.cat.ai.workchain.common.{InProcess, Listener}
import io.cat.ai.workchain.core.scheduler.manager.JobSetManager

import scala.annotation.tailrec

final class ParallelDagJobExecutor(override val scheduler: AbstractDagJobScheduler,
                                   override val poolSize: Int,
                                   override val parallelismLevel: Int)
                                  (implicit val parallelizerPool: ExecutorService,
                                   implicit val workerPool: ExecutorService) extends AbstractDagJobExecutor with ManageableParallelizableDagExecutorGroup {

  private val executors: Seq[ExecutorService] = parallelizerPool :: workerPool :: Nil

  protected override val running: AtomicBoolean = new AtomicBoolean(true)

  override def hasJobs: Boolean = managerCache.values.map(_.jobSetManager).count(_.hasUncompletedJobs) > 0

  override def inProcess: Seq[Runnable] = {
    for ((job, state) <- managerCache.values.flatMap(_.jobState) if state == InProcess)
      yield job
  }.toSeq

  override def shutdown(): Unit = {
    for (exec <- executors) exec shutdownNow()
    running := false
  }

  @tailrec
  override def runRecLoop(uuid: String, jobSetManager: JobSetManager[Job], condition: Boolean): Unit =
    if (condition) {
      jobSetManager.takeJob match {
        case Some(job) => execute(uuid, job)

        case None      => // runs in O(1)
          managerCache.get(uuid) match {
            case Some(dagJobScheduler) => if (hasJobs && dagJobScheduler.jobsWithError.nonEmpty) ()
            case _                     => ()
          }
      }
      runRecLoop(uuid, jobSetManager, is(running))
    } else ()

  override def submit(dag: DAG[Job, Edge[Job]]): Unit = parallelizerPool execute { () =>
    implicit val listener: Listener[String, Job] = WorkChainEnv.createListener(managerCache)
    val dagSchedulerManager = scheduler.schedule(dag)

    val uuid = randUUID

    managerCache += uuid -> dagSchedulerManager

    workerPool execute { () => runRecLoop(uuid, dagSchedulerManager.jobSetManager, is(running)) }
  }

  override def isActive: Boolean = is(running)
}