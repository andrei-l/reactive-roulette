package com.github.al.roulette.load.impl

import akka.actor.{Cancellable, Scheduler}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class ThrottlingAccumulatorSpec extends WordSpec with Matchers {
  private val throttlingAccumulator = ThrottlingAccumulator(DummyScheduler, _ => {})(ExecutionContext.Implicits.global)

  "The ThrottlingAccumulator" should {
    "accumulate identical messages" in {
      throttlingAccumulator.accumulateIdenticalMessages(
        List("aa", "aa", "aa", "bb", "bb", "cc", "aa")
      ) should contain theSameElementsInOrderAs List("x3 aa", "x2 bb", "cc", "aa")
    }
  }

  private object DummyScheduler extends Scheduler {
    override def schedule(initialDelay: FiniteDuration, interval: FiniteDuration, runnable: Runnable)(implicit executor: ExecutionContext): Cancellable = ???

    override def scheduleOnce(delay: FiniteDuration, runnable: Runnable)(implicit executor: ExecutionContext): Cancellable = ???

    override def maxFrequency: Double = ???
  }

}