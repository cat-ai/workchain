package io.cat.ai.workchain.app.executor.internal

import java.util
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

import io.cat.ai.workchain.common.AtomicOps.BooleanOps._
import io.cat.ai.workchain.core.executor.internal.{FastExecutorService, FastExecutorServiceWorker}

import org.jctools.queues.SpmcArrayQueue

import scala.collection.mutable

final class JcTFastExecutorService(override val threadQueueCapacity: Int,
                                   override val serviceName: String) extends FastExecutorService {

  private val running: AtomicBoolean = new AtomicBoolean(true)

  override val threads: mutable.ArrayBuffer[Thread] = new mutable.ArrayBuffer[Thread]
  override val taskQueue: java.util.Queue[Runnable] = new SpmcArrayQueue[Runnable](threadQueueCapacity)

  {
    def createAddAndStart(threadName: String): Unit = {
      val thread = new Thread(new ExecutorServiceWorker(running), threadName)
      threads += thread
      thread.start()
    }

    if (threadQueueCapacity == 0 || threadQueueCapacity == 1) createAddAndStart(s"work-chain-thread-$serviceName-0")
    else for (n <- 0 to threadQueueCapacity) createAddAndStart(s"work-chain-thread-$serviceName-$n")
  }

  override def execute(command: Runnable): Unit =
    while (!taskQueue.offer(command))
      LockSupport.parkNanos(1)

  private class ExecutorServiceWorker (override val running: AtomicBoolean) extends FastExecutorServiceWorker {

    override def run(): Unit =
      while (is(running)) {
        var task = null.asInstanceOf[Runnable]
        var innerCond: Boolean = true

        while (innerCond) {
          if (Thread.interrupted()) return

          task = taskQueue.poll()

          if (task ne null) innerCond = false
          if (innerCond) LockSupport.parkNanos(1)
        }

        try
          if (task ne null)
            task.run()
        catch {
          case ex: Exception => ex.printStackTrace()
        }
      }
  }

  override def shutdown(): Unit = {
    for (thread <- threads) thread.interrupt()

    threads.clear()
    running := false
  }

  override def shutdownNow(): util.List[Runnable] = {
    shutdown()
    new util.ArrayList[Runnable](taskQueue)
  }
  override def isShutdown: Boolean = is(running)

  override def isTerminated: Boolean = isShutdown
}