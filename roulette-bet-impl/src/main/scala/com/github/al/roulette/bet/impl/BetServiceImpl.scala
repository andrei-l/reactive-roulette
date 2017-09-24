package com.github.al.roulette.bet.impl

import java.util.UUID

import akka.NotUsed
import com.github.al.authentication.AuthenticatedServerServiceCall.authenticated
import com.github.al.logging.EventStreamLogging
import com.github.al.logging.LoggedServerServiceCall.logged
import com.github.al.persistence.PersistentEntityRegistrySugar
import com.github.al.roulette.bet.api
import com.github.al.roulette.bet.api._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class BetServiceImpl(override val entityRegistry: PersistentEntityRegistry)(implicit val executionContext: ExecutionContext)
  extends BetService
    with PersistentEntityRegistrySugar
    with EventStreamLogging
    with LazyLogging {

  override def placeBet(gameId: UUID): ServiceCall[Bet, NotUsed] =
    authenticated { userId =>
      logged {
        ServerServiceCall { bet =>
          entityRef[RouletteGameBetsEntity](gameId)
            .ask(PlaceBet(userId, bet))
            .map(_ => NotUsed)
        }
      }
    }

  override def rouletteBetsEvents: Topic[RouletteBetsEvent] = TopicProducer.singleStreamWithOffset { offset =>
    entityRegistry.eventStream(RouletteGameBetsEvent.Tag, offset)
      .filter(_.event.isInstanceOf[GameBettingFinished]).mapAsync(1)(logEventStreamElementAsync).mapAsync(1) {
      case EventStreamElement(gameId, GameBettingFinished(playerBets), _offset) =>
        Future.successful(AllGameBetsProclaimed(gameId, toApiPlayersBets(playerBets)) -> _offset)
    }
  }

  private def toApiPlayersBets(playerBets: List[PlayerBet]): List[PlayerBets] =
    playerBets.groupBy(_.playerId).view.map { case (playerId, bets) => api.PlayerBets(playerId, bets.map(_.bet)) } toList
}
