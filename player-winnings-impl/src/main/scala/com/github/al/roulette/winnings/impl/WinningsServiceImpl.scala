package com.github.al.roulette.winnings.impl

import com.github.al.persistence.UUIDConversions
import com.github.al.roulette.winnings.api
import com.github.al.roulette.winnings.api.WinningsService
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.concurrent.Future

class WinningsServiceImpl(entityRegistry: PersistentEntityRegistry)
  extends WinningsService
    with UUIDConversions {

  override def winningsEvents: Topic[api.WinningsEvent] = TopicProducer.singleStreamWithOffset { offset =>
    entityRegistry.eventStream(WinningsEvent.Tag, offset)
      .filter(_.event.isInstanceOf[WinningsCalculated]).mapAsync(1) {
      case EventStreamElement(gameId, WinningsCalculated(playerWinnings), _offset) =>
        Future.successful(api.WinningsCalculated(gameId, playerWinnings) -> _offset)
    }
  }
}
