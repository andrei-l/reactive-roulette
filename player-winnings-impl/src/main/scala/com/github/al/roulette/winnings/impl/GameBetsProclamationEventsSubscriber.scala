package com.github.al.roulette.winnings.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.github.al.persistence.PersistentEntityRegistrySugar
import com.github.al.roulette.bet.api.{AllGameBetsProclaimed, BetService, RouletteBetsEvent}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.{ExecutionContext, Future}

class GameBetsProclamationEventsSubscriber(betService: BetService,
                                           override val entityRegistry: PersistentEntityRegistry)(implicit executionContext: ExecutionContext)
  extends PersistentEntityRegistrySugar {

  betService.rouletteBetsEvents.subscribe.atLeastOnce(Flow[RouletteBetsEvent].mapAsync(1) {
    case AllGameBetsProclaimed(gameId, playersBets) => entityRef[WinningsEntity](gameId).ask(SavePlayersBets(playersBets))
    case _ => Future(Done)
  })
}
