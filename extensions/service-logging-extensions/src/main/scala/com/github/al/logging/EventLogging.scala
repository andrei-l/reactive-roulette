package com.github.al.logging

import com.typesafe.scalalogging.Logger

import scala.concurrent.Future

trait EventLogging {
  protected val logger: Logger

  def logEventAsync[T](event: T): Future[T] = {
    logger.debug(s"Received $event")
    Future.successful(event)
  }
}
