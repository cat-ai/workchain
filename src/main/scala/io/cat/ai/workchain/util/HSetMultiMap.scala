package io.cat.ai.workchain.util

import MultiMap._

/**
 * Since HashSet used as internal data structure for values, the [[scala.collection.mutable.HashSet.contains()]] runs in O(1),
 * operations like [[removeValue()]] and [[putValue()]] runs in O(N)
 * */
final class HSetMultiMap[A, B] extends AbstractHSetMultiMap[A, B] { self =>

  override def size(): Int = values().size

  override def isEmpty: Boolean = underlyingMap.isEmpty

  override def remove(k: A): Option[MutSetB] = underlyingMap.remove(k)

  override def remove(key: A, value: B): self.type = {
    underlyingMap.removeBinding(key, value)
    self
  }

  override def keySet(): MutSetA = underlyingMap.keySet.to[MutSet]

  override def contains(key: A): Boolean = underlyingMap.contains(key)

  override def clear(): Unit = underlyingMap.clear()

  override def get(key: A): Option[MutSetB] = underlyingMap.get(key)

  /**
   * Complexity O(N)
   * */
  override def putValue(k: A, v: B): self.type = {
    underlyingMap.addBinding(k, v)
    self
  }
  
  override def put(key: A, value: MutSetB): Option[MutSetB] = underlyingMap.put(key, value)

  /**
   * Complexity O(N)
   **/
  override def removeValue(value: B): self.type = {
    for ((_, set) <- underlyingMap if set contains value)
      set -= value
    self
  }

  override def removeValue(key: A, value: B): MultiMap[A, B] = {
    underlyingMap.removeBinding(key, value)
    self
  }

  override def containsValue(value: B): Boolean = values().flatten contains value

  override def values(): MutSet[MutSetB] = underlyingMap.values.to[MutSet]

  override def +=(kv: (A, MutSet[B])): self.type = {
    val (key, value) = kv
    put(key, value)
    self
  }

  override def -=(key: A): self.type = {
    remove(key)
    self
  }

  override def iterator: Iterator[(A, MutSet[B])] = underlyingMap.iterator
}

object HSetMultiMap {
  def empty[A, B]: HSetMultiMap[A, B] = new HSetMultiMap[A, B]
}