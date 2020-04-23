package io.cat.ai.workchain.core.graph.directed

import io.cat.ai.workchain.core.graph.Graph
import io.cat.ai.workchain.core.graph.exceptions.GraphException

import org.jgrapht.Graphs
import org.jgrapht.alg.cycle.CycleDetector
import org.jgrapht.graph.{DirectedAcyclicGraph => JGraphTDAG}

import collection.JavaConverters._

import scala.reflect.ClassTag

final class DirectedAcyclicGraph[V, E : ClassTag] extends AbstractDirectedAcyclicGraph[V, E] { self =>

  object Implicits {

    implicit class EdgeTP(source: V) {
      def *-->(target: V): self.type = {
        self.connect(source, target)
        self
      }
    }
  }

  private val underlyingGraph: JGraphTDAG[V, E] = new JGraphTDAG[V, E](implicitly[ClassTag[E]].runtimeClass.asInstanceOf[Class[E]])

  private lazy val cycleDetector: CycleDetector[V, E] = new CycleDetector(underlyingGraph)

  private def fail(exc: Exception): Graph[V, E] = throw exc

  override def vertices(): Set[V] = underlyingGraph.vertexSet().asScala.to[Set]

  override def removeVertices(vertices: Seq[V]): Unit = underlyingGraph.removeAllVertices(vertices.asJava)

  override def removeVertices(): Unit = for (vertex <- vertices()) underlyingGraph.removeVertex(vertex)
  
  override def incomingEdges(vertex: V): Set[E] = underlyingGraph.incomingEdgesOf(vertex).asScala.to[Set]

  override def outgoingEdges(vertex: V): Set[E] = underlyingGraph.outgoingEdgesOf(vertex).asScala.to[Set]

  override def successors(vertex: V): Seq[V] = Graphs.successorListOf(underlyingGraph, vertex).asScala

  override def predecessors(vertex: V): Seq[V] = Graphs.predecessorListOf(underlyingGraph, vertex).asScala

  override def addVertex(a: V): Graph[V, E] =
    try {
      underlyingGraph.addVertex(a)
      self
    } catch {
      case _: NullPointerException => fail(new GraphException("Provided vertex is null"))
    }

  override def removeVertex(a: V): Graph[V, E] = {
    underlyingGraph.removeVertex(a)
    self
  }

  private def connect0(source: V, target: V): Graph[V, E] =
    try {
      underlyingGraph.addEdge(source, target)
      self
    } catch {
      case ex: IllegalArgumentException => fail(new GraphException(s"Parent exception [${ex.toString}] on connect operation :: source: $source - target: $target]"))
    }

  override def connect(source: V, target: V): Graph[V, E] = connect0(source, target)

  override def removeConnection(edge: E): Graph[V, E] = {
    underlyingGraph.removeEdge(edge)
    self
  }

  override def removeConnection(sourceVertex: V, target: V): Graph[V, E] = {
    underlyingGraph.removeEdge(sourceVertex, target)
    self
  }

  override def connections: Set[E] = underlyingGraph.edgeSet().asScala.to[Set]

  override def isDirected: Boolean = underlyingGraph.getType.isDirected

  override def addVertices(vertices: Seq[V]): Graph[V, E] = {
    for (vertex <- vertices)
      addVertex(vertex)
    self
  }

  override def hasCycle: Boolean = cycleDetector.detectCycles()

  override def containsVertex(vertex: V): Boolean = underlyingGraph.containsVertex(vertex)
}