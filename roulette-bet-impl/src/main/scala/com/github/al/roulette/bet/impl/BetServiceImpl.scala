package com.github.al.roulette.bet.impl

import java.util.UUID

import akka.NotUsed
import com.github.al.persistence.PersistentEntityRegistrySugar
import com.github.al.roulette.bet.api.{Bet, BetService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext

class BetServiceImpl(override val entityRegistry: PersistentEntityRegistry)(implicit val executionContext: ExecutionContext)
  extends BetService
    with PersistentEntityRegistrySugar {

  override def placeBet(gameId: UUID): ServiceCall[Bet, NotUsed] = ServiceCall { bet =>
    entityRef[RouletteGameBetsEntity](gameId)
      .ask(PlaceBet(???, bet))
      .map(_ => NotUsed)
  }
}
