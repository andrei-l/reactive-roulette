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
    drained.asScala
      .map(_ -> 1)
      .sliding(2)
      .collect {
        case Seq((a, aN), (b, bN)) if a == b => a -> (aN + bN)
        case Seq(a, b) => b
      }
      .map {
        case (a, aN) if aN == 1 => a
        case (a, aN) => s"${aN}x $a"
      }
      .foreach(flush)
    scheduled = false
  }

}
