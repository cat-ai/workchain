package io.cat.ai.workchain

import java.util.concurrent.ExecutorService

import io.cat.ai.workchain.app.executor.internal.JcTFastExecutorService
import io.cat.ai.workchain.app.executor.{AbstractDagJobExecutor, MultithreadedDagJobExecutor, ParallelDagJobExecutor, SingleThreadedDagJobExecutor}
import io.cat.ai.workchain.app.scheduler.{DefaultDagJobScheduler, DynamicModeDagJobScheduler, DynamicModeScheduler}
import io.cat.ai.workchain.app.scheduler.manager.DagJobSchedulerManager
import io.cat.ai.workchain.common.Listener
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.executor.internal.FastExecutorService
import io.cat.ai.workchain.core.scheduler.AbstractDagJobScheduler
import io.cat.ai.workchain.core.scheduler.manager.{DagSchedulerManager, JobSetManagerMode}

import scala.collection.mutable.{Set => MutSet}

package object app {

  object executors {

    object service {

      private def fastExecutorService(poolSize: Int, name: String): FastExecutorService = new JcTFastExecutorService(poolSize, name)

      lazy val singleThreadedFastExecutorService: ExecutorService = fastExecutorService(1, "worker")

      def newMultithreadedFastExecutorService(poolSize: Int): ExecutorService = fastExecutorService(poolSize, "worker")

      def newMultithreadedFastExecutorServices(poolSize: Int, parallelismLevel: Int): (ExecutorService, ExecutorService) =
        fastExecutorService(poolSize + 1, "worker") -> fastExecutorService(poolSize * parallelismLevel, "parallelizer")
    }

    def singleThreaded(jobScheduler: AbstractDagJobScheduler): AbstractDagJobExecutor =
      new SingleThreadedDagJobExecutor(jobScheduler)(workerPool = service.singleThreadedFastExecutorService)

    def multithreaded(jobScheduler: AbstractDagJobScheduler,
                      poolSize: Int): AbstractDagJobExecutor =
      new MultithreadedDagJobExecutor(jobScheduler, poolSize)(workerPool = service.newMultithreadedFastExecutorService(poolSize))

    def multithreaded(jobScheduler: AbstractDagJobScheduler)
                     (implicit executorService: FastExecutorService): AbstractDagJobExecutor =
      new MultithreadedDagJobExecutor(jobScheduler, executorService.threadQueueCapacity)(workerPool = executorService)

    def multithreadedParallel(jobScheduler: AbstractDagJobScheduler,
                              poolSize: Int,
                              parallelismLevel: Int)
                             (implicit parallelizerPool: ExecutorService, workerPool: ExecutorService): AbstractDagJobExecutor =
      new ParallelDagJobExecutor(jobScheduler, poolSize, parallelismLevel)(parallelizerPool = parallelizerPool, workerPool = workerPool)
  }

  object schedulers {

    private lazy val defaultDagJobSchedulerInstance: AbstractDagJobScheduler = new DefaultDagJobScheduler

    object managers {

      def dagSchedulerManager(jobSetManagerMode: JobSetManagerMode)
                             (implicit listener: Listener[String, Job]): DagSchedulerManager[Job, Throwable, MutSet] = new DagJobSchedulerManager(jobSetManagerMode)
    }

    def defaultDagJobScheduler(): AbstractDagJobScheduler = defaultDagJobSchedulerInstance

    def newDefaultDagJobScheduler(): AbstractDagJobScheduler = new DefaultDagJobScheduler

    def dynamicModeDagJobScheduler(mode: JobSetManagerMode): AbstractDagJobScheduler with DynamicModeScheduler = new DynamicModeDagJobScheduler(mode)
  }
}
