package io.cat.ai.workchain.core.executor

import io.cat.ai.workchain.concurrent.ManageableExecutorWithDelayGroup
import io.cat.ai.workchain.concurrent.job.Job
import io.cat.ai.workchain.core.graph.{DAG, Edge}

trait ManageableDagExecutorWithDelayGroup extends DagExecutorGroup with ManageableExecutorWithDelayGroup[DAG[Job, Edge[Job]]]