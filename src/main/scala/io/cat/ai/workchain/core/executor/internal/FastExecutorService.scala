package io.cat.ai.workchain.core.executor.internal

import java.util
import java.util.concurrent.{Callable, ExecutorService, Future, TimeUnit}

import scala.collection.mutable

trait FastExecutorService extends ExecutorService {

  override def execute(command: Runnable): Unit

  override def shutdown(): Unit

  override def isShutdown: Boolean

  override def isTerminated: Boolean

  def serviceName: String

  def threads: mutable.Seq[Thread]

  def taskQueue: util.Queue[_ <: Runnable]

  def threadQueueCapacity: Int

  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = throw new UnsupportedOperationException("ExecutorService#submit")

  override def submit[T](task: Callable[T]): Future[T] = throw new UnsupportedOperationException("ExecutorService#submit")

  override def submit[T](task: Runnable, result: T): Future[T] = throw new UnsupportedOperationException("ExecutorService#submit")

  override def submit(task: Runnable): Future[_] = throw new UnsupportedOperationException("ExecutorService#submit")

  override def invokeAll[T](tasks: util.Collection[_ <: Callable[T]]): util.List[Future[T]] = throw new UnsupportedOperationException("ExecutorService#invokeAll")

  override def invokeAll[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): util.List[Future[T]] = throw new UnsupportedOperationException("ExecutorService#invokeAll")

  override def invokeAny[T](tasks: util.Collection[_ <: Callable[T]]): T = throw new UnsupportedOperationException("ExecutorService#invokeAny")

  override def invokeAny[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T = throw new UnsupportedOperationException("ExecutorService#invokeAny")
}