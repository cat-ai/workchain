package io.cat.ai.workchain.util

import scala.collection.mutable

import MultiMap._

abstract class AbstractHSetMultiMap[A, B] extends MultiMap[A, B] { self =>

  protected type MutSetA = MutSet[A]
  protected type MutSetB = MutSet[B]

  protected type MutHSetB = MutSet[B]
  protected type MutHM =  MutHMap[A, MutSetB]
  protected type MutMM = mutable.MultiMap[A, B]

  final val underlyingMap: MutHM with MutMM = new MutHM with MutMM

  def removeValue(key: A, value: B): MultiMap[A, B]

  def removeValue(value: B): MultiMap[A, B]

  def putValue(k: A, v: B): MultiMap[A, B]

  def containsValue(value: B): Boolean

  override def toString: String = s"Multi$underlyingMap"
}