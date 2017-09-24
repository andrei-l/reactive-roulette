package com.github.al.roulette.bet.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.github.al.logging.EventLogging
import com.github.al.persistence.PersistentEntityRegistrySugar
import com.github.al.roulette.game
import com.github.al.roulette.game.api.{GameEvent, GameService}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

class GameEventsSubscriber(gameService: GameService,
                           override val entityRegistry: PersistentEntityRegistry)(implicit executionContext: ExecutionContext)
  extends PersistentEntityRegistrySugar
    with EventLogging
    with LazyLogging {

  gameService.gameEvents.subscribe.atLeastOnce(Flow[GameEvent].mapAsync(1)(logEventAsync).mapAsync(1) {
    case e: game.api.GameStarted => entityRef[RouletteGameBetsEntity](e.gameId).ask(StartGameBetting)
    case e: game.api.GameFinished => entityRef[RouletteGameBetsEntity](e.gameId).ask(FinishGameBetting)
    case _ => Future.successful(Done)
  })
}
