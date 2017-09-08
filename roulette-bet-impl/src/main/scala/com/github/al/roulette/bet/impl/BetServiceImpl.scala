package com.github.al.roulette.bet.impl

import java.util.UUID

import akka.NotUsed
import com.github.al.persistence.PersistentEntityRegistrySugar
import com.github.al.roulette.bet.api.{Bet, BetService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.github.al.authentication.AuthenticatedServerServiceCall.authenticated
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.ExecutionContext

class BetServiceImpl(override val entityRegistry: PersistentEntityRegistry)(implicit val executionContext: ExecutionContext)
  extends BetService
    with PersistentEntityRegistrySugar {

  override def placeBet(gameId: UUID): ServiceCall[Bet, NotUsed] = authenticated { userId =>
    ServerServiceCall { bet =>
      entityRef[RouletteGameBetsEntity](gameId)
        .ask(PlaceBet(userId, bet))
        .map(_ => NotUsed)
    }
  }
}
