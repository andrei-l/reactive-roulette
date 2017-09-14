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
    if (!scheduled) scheduler.scheduleOnce(5 second)(flushQueue())

  }

  private def flushQueue(): Unit = {
    scheduled = true
    val drained = new util.ArrayList[String]()
    queue.drainTo(drained)
    drained.asScala.foreach(flush)
  }

}
