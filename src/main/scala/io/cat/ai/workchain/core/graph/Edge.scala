package io.cat.ai.workchain.core.graph

import org.jgrapht.graph.DefaultEdge

final case class Edge[V]() extends DefaultEdge {

  def edgeSource: V = getSource.asInstanceOf[V]

  def edgeTarget: V = getTarget.asInstanceOf[V]
}