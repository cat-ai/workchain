package io.cat.ai.workchain.app

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import io.cat.ai.workchain.app.executor.AbstractDagJobExecutor
import io.cat.ai.workchain.app.scheduler.DynamicModeScheduler
import io.cat.ai.workchain.core.executor.{ExecutionMode, MultiThreaded, ParallelMultiThreaded, SingleThreaded}
import io.cat.ai.workchain.core.scheduler.AbstractDagJobScheduler
import io.cat.ai.workchain.core.scheduler.manager.{JobSetManagerMode, LockMode, SynchronizedMode}

import scala.collection.mutable

trait WorkChain {

  def id: String

  def executorGroup: AbstractDagJobExecutor

  def executionMode: ExecutionMode

  def schedulersJobSetManagerMode: JobSetManagerMode

  def applyExecutionMode(newMode: ExecutionMode): Unit

  def applySchedulersJobSetManagerMode(newMode: JobSetManagerMode): Unit

  def sessions: mutable.Map[String, WorkChainSession]

  def createNewSession(): WorkChainSession
}

protected class DefaultWorkChain (override val id: String) extends WorkChain {

  private val executionModeAtomic: AtomicReference[ExecutionMode] = new AtomicReference[ExecutionMode](SingleThreaded)
  private val schedulersJobSetManagerModeAtomic: AtomicReference[JobSetManagerMode] = new AtomicReference[JobSetManagerMode](LockMode)
  private val executorGroupAtomic: AtomicReference[AbstractDagJobExecutor] = new AtomicReference[AbstractDagJobExecutor](executors.singleThreaded(scheduler))

  private val sessionsMap: mutable.Map[String, WorkChainSession] = new mutable.HashMap[String, WorkChainSession]

  override def executorGroup: AbstractDagJobExecutor = executorGroupAtomic.get()

  override def executionMode: ExecutionMode = executionModeAtomic.get()

  private def scheduler: AbstractDagJobScheduler with DynamicModeScheduler = schedulers.dynamicModeDagJobScheduler(schedulersJobSetManagerModeAtomic.get())

  override def applyExecutionMode(newExMode: ExecutionMode): Unit = {
    executorGroup.shutdown()
    newExMode match {
      case SingleThreaded                               => executorGroupAtomic.set(executors.singleThreaded(scheduler))

      case MultiThreaded(nThreads)                      => executorGroupAtomic.set(executors.multithreaded(scheduler, nThreads))

      case ParallelMultiThreaded(nThreads, parallelism) =>
        val (parallelizerPool, workerPool) = executors.service.newMultithreadedFastExecutorServices(nThreads, parallelism)
        executorGroupAtomic.set(executors.multithreadedParallel(scheduler, nThreads, parallelism)(parallelizerPool = parallelizerPool, workerPool = workerPool))
    }
    executionModeAtomic.set(newExMode)
  }

  override def applySchedulersJobSetManagerMode(newJobSetManagerMode: JobSetManagerMode): Unit = {
    newJobSetManagerMode match {
      case lock @ LockMode         => executorGroup.scheduler.asInstanceOf[AbstractDagJobScheduler with DynamicModeScheduler].applyJobSetManagerMode(lock)
      case sync @ SynchronizedMode => executorGroup.scheduler.asInstanceOf[AbstractDagJobScheduler with DynamicModeScheduler].applyJobSetManagerMode(sync)
    }
    schedulersJobSetManagerModeAtomic.set(newJobSetManagerMode)
  }

  override def createNewSession(): WorkChainSession = {
    val taskSession: WorkChainSession = WorkChainSession.createDefaultSession(exMode = executionMode, exGroup = executorGroup)
    sessionsMap += taskSession.sessionId -> taskSession
    taskSession
  }

  override def sessions: mutable.Map[String, WorkChainSession] = sessionsMap

  override def schedulersJobSetManagerMode: JobSetManagerMode = schedulersJobSetManagerModeAtomic.get()
}

object WorkChain {

  private def appId: String = s"WorkChainApp-${UUID.randomUUID}"

  private lazy val defaultInstance: DefaultWorkChain = new DefaultWorkChain(appId)

  def apply(): DefaultWorkChain = defaultInstance
}