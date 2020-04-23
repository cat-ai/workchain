package io.cat.ai.workchain.util

import scala.collection.mutable

import MultiMap._

trait MultiMap[A, B] extends mutable.Map[A, MutSet[B]] {

  def size(): Int

  def isEmpty: Boolean

  override def nonEmpty: Boolean = !isEmpty

  override def get(key: A): Option[MutSet[B]]

  def remove(k: A): Option[MutSet[B]]

  def remove(key: A, value: B): MultiMap[A, B]

  def contains(key: A): Boolean

  def clear(): Unit
}

object MultiMap {
  type MutSet[A] = mutable.Set[A]
  type MutHSet[A] =  mutable.HashSet[A]
  type MutMap[A, B] =  mutable.Map[A, B]
  type MutHMap[A, B] =  mutable.HashMap[A, B]
  type JMap[K, V] = java.util.Map[K, V]
  type JLHMap[K, V] = java.util.LinkedHashMap[K, V]
}