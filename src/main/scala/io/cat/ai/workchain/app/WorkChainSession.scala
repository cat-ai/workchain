package io.cat.ai.workchain.app

import io.cat.ai.workchain.app.api.{TaskWrapper, TaskWrapperNRet, TaskWrapperNRetRec, TaskWrapperRet, TaskWrapperRetRec}
import io.cat.ai.workchain.app.executor.AbstractDagJobExecutor
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.executor.ExecutionMode

import scala.collection.mutable

trait WorkChainSession {

  def sessionId: String

  def workChainContext: WorkChainContext

  def taskCache: mutable.Map[TaskWrapper, Job]

  def executionMode: ExecutionMode

  def executorGroup: AbstractDagJobExecutor

  def addTask(wrapper: TaskWrapper): Unit

  def addTasks(wrapper: TaskWrapper*): Unit

  def chainTasks(sourceWrapper: TaskWrapper, targetWrapper: TaskWrapper): Unit

  def chainTasks(chainedWrappers: (TaskWrapper, TaskWrapper)): Unit

  def chainTasks(chainedWrappers: (TaskWrapper, TaskWrapper)*): Unit

  def compute(): Unit

  def destroy(): Unit

  object utils {

    def unwrapAndCreateJob(taskWrapper: TaskWrapper): Job = taskWrapper match {
      case TaskWrapperRet(task)              => Job.retryable(task)
      case TaskWrapperNRet(task)             => Job.nonRetryable(task)
      case TaskWrapperRetRec(task, recover)  => Job.retryableWithRecover(task, recover)
      case TaskWrapperNRetRec(task, recover) => Job.nonRetryableWithRecover(task, recover)
    }

    def addJobToDAG(job: Job): Unit = workChainContext.jobGraph.addVertex(job)

    def addConnectionToDAG(sourceWrapper: TaskWrapper, targetWrapper: TaskWrapper): Unit =
      for {
        source <- taskCache.get(sourceWrapper)
        target <- taskCache.get(targetWrapper)
        _      =  addJobToDAG(source)
        _      =  addJobToDAG(target)
      } workChainContext.jobGraph.connect(source, target)
  }

  object Implicits {

    import utils._

    implicit class TaskWrapperS(sourceWrapper: TaskWrapper) {
      def *-->(targetWrapper: TaskWrapper): Unit = addConnectionToDAG(sourceWrapper, targetWrapper)
    }
  }
}

private class DefaultWorkChainSession(override val sessionId: String,
                                      override val executionMode: ExecutionMode,
                                      override val executorGroup: AbstractDagJobExecutor) extends WorkChainSession {

  import utils._

  override val taskCache: mutable.Map[TaskWrapper, Job] = new mutable.HashMap[TaskWrapper, Job]
  override val workChainContext: WorkChainContext = WorkChainContext.createDefaultContext(executionMode, executorGroup)

  override def addTask(wrapper: TaskWrapper): Unit = {
    val job = unwrapAndCreateJob(wrapper)
    taskCache += wrapper -> job
    addJobToDAG(job)
  }

  override def addTasks(wrappers: TaskWrapper*): Unit = for (wrapper <- wrappers) addTask(wrapper)

  override def chainTasks(sourceWrapper: TaskWrapper, targetWrapper: TaskWrapper): Unit = addConnectionToDAG(sourceWrapper, targetWrapper)

  override def chainTasks(chainedWrappers: (TaskWrapper, TaskWrapper)): Unit = chainTasks(chainedWrappers._1, chainedWrappers._2)

  override def chainTasks(chainedWrappers: (TaskWrapper, TaskWrapper)*): Unit = for ((sourceWrapper, edgeWrapper) <- chainedWrappers) chainTasks(sourceWrapper, edgeWrapper)

  override def compute(): Unit = workChainContext.startExecution()

  override def destroy(): Unit = workChainContext.executorGroup.shutdown()
}

object WorkChainSession {

  import java.util.UUID

  def generateId: String = s"TaskSession-${UUID.randomUUID}"

  def createDefaultSession(sessionId: String = generateId,
                           exMode: ExecutionMode,
                           exGroup: AbstractDagJobExecutor): WorkChainSession = new DefaultWorkChainSession(sessionId, exMode, exGroup)
}
