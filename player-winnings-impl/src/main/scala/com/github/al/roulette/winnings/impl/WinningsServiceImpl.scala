package com.github.al.roulette.winnings.impl

import com.github.al.persistence.UUIDConversions
import com.github.al.roulette.winnings.api
import com.github.al.roulette.winnings.api.WinningsService
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

class WinningsServiceImpl(entityRegistry: PersistentEntityRegistry)
  extends WinningsService
    with UUIDConversions with LazyLogging {

  override def winningsEvents: Topic[api.WinningsEvent] = TopicProducer.singleStreamWithOffset { offset =>
    entityRegistry.eventStream(WinningsEvent.Tag, offset)
      .filter(_.event.isInstanceOf[WinningsCalculated]).map(logWinningsEvent).mapAsync(1) {
      case EventStreamElement(gameId, WinningsCalculated(playerWinnings), _offset) =>
        Future.successful(api.WinningsCalculated(gameId, playerWinnings) -> _offset)
    }
  }
  private def logWinningsEvent[T <: EventStreamElement[WinningsEvent]](eventElement: T): T = {
    logger.info(s"Triggered ${eventElement.event} for game ${eventElement.entityId}")
    eventElement
  }

}
