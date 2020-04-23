package io.cat.ai.workchain.core.graph

trait Graph[V, E] {

  def vertices(): Set[V]

  def incomingEdges(vertex: V): Set[E]

  def outgoingEdges(vertex: V): Set[E]

  def successors(vertex: V): Seq[V]

  def predecessors(vertex: V): Seq[V]

  def addVertex(a: V): Graph[V, E]

  def addVertices(vertices: Seq[V]): Graph[V, E]

  def removeVertex(a: V): Graph[V, E]

  def removeVertices(): Unit

  def removeVertices(vertices: Seq[V]): Unit

  def connect(source: V, target: V): Graph[V, E]

  def removeConnection(edge: E): Graph[V, E]

  def removeConnection(sourceVertex: V, target: V): Graph[V, E]

  def connections: Set[E]

  def containsVertex(vertex: V): Boolean
}