package com.github.al.roulette.load.impl

import java.util
import java.util.concurrent.LinkedBlockingQueue

import akka.actor.Scheduler

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationDouble
import scala.language.postfixOps

private[impl] case class ThrottlingAccumulator(scheduler: Scheduler, flush: String => Unit)
                                              (implicit executionContext: ExecutionContext) {
  private val queue: LinkedBlockingQueue[String] = new LinkedBlockingQueue[String]()
  @volatile var scheduled = false

  def enqueue(msg: String): Unit = {
    queue.add(msg)
    if (!scheduled) {
      scheduled = true
      scheduler.scheduleOnce(1 second)(flushQueue())
    }
  }

  private def flushQueue(): Unit = {
    val drained = new util.ArrayList[String]()
    queue.drainTo(drained)
    accumulateIdenticalMessages(drained.asScala.toList).foreach(flush)
    scheduled = false
  }

  private[impl] def accumulateIdenticalMessages(messages: List[String]): List[String] =
    (List(messages.take(1)) /: messages.tail) ((l, r) =>
      if (l.head.head == r) (r :: l.head) :: l.tail else List(r) :: l
    ).reverseMap(_.reverse).map {
      case xs@List(head) => head
      case xs@head :: _ => s"x${xs.size} $head"
    }
}
