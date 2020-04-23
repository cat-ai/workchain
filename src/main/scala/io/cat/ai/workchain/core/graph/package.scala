package io.cat.ai.workchain.core

import io.cat.ai.workchain.core.graph.directed.DirectedAcyclicGraph

import scala.reflect.ClassTag

package object graph {

  type DAG[V, E] = DirectedAcyclicGraph[V, E]

  object dag {

    def empty[V, E : ClassTag]: DirectedAcyclicGraph[V, E] = new DAG[V, E]

    def one[V, E : ClassTag](vertex: V): DirectedAcyclicGraph[V, E] = {
      val graph = empty[V, E]
      graph addVertex vertex
      graph
    }

    def fromSeq[V, E : ClassTag](seq: Seq[V]): DirectedAcyclicGraph[V, E] = {
      val graph = empty[V, E]
      graph addVertices seq
      graph
    }

    def fromSet[V, E : ClassTag](set: Set[V]): DirectedAcyclicGraph[V, E] = fromSeq(set.toSeq)
  }
}