package io.cat.ai.workchain.core.graph.directed

import io.cat.ai.workchain.core.graph.Graph

trait DirectedGraph[V, E] extends Graph[V, E] {
  def isDirected: Boolean
}
