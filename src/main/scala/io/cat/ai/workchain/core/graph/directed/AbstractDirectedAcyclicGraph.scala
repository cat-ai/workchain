package io.cat.ai.workchain.core.graph.directed

abstract class AbstractDirectedAcyclicGraph[V, E] extends DirectedGraph[V, E] {

  def hasCycle: Boolean

  override def isDirected: Boolean
}