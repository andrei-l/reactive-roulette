package com.github.al.logging

import com.lightbend.lagom.scaladsl.persistence.EventStreamElement
import com.typesafe.scalalogging.Logger

import scala.concurrent.Future

trait EventStreamLogging {
  protected val logger: Logger

  def logEventStreamElementAsync[T <: EventStreamElement[_]](eventElement: T): Future[T] = {
    logger.debug(s"Received EventStreamElement with ${eventElement.event} for entity ${eventElement.entityId}")
    Future.successful(eventElement)
  }
}
