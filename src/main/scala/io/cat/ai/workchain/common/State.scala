package io.cat.ai.workchain.common

sealed trait State

case object Init extends State

case object InProcess extends State

case object Done extends State

case object Retry extends State
case object Recover extends State

case object Fail extends State
case object Error extends State