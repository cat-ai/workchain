package io.cat.ai.workchain.core.scheduler

import io.cat.ai.workchain.common.Listener
import io.cat.ai.workchain.core.graph.DAG

trait DagScheduler[V, E, M] {

  def schedule(dag: DAG[V, E])(implicit listener: Listener[String, V]): M
}