package com.github.al.persistence

import akka.actor.Scheduler

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import akka.pattern.after

trait Retrying {
  def retry[T](f: => Future[T], delay: FiniteDuration, timeout: FiniteDuration)
              (implicit ec: ExecutionContext, s: Scheduler): Future[T] = {
    f recoverWith { case _ if timeout.toMillis >= 0 => after(delay, s)(retry(f, delay, timeout - delay)) }
  }
}

