package com.github.al.roulette.player.impl

import java.util.UUID

import akka.NotUsed
import akka.actor.Scheduler
import com.github.al.logging.EventStreamLogging
import com.github.al.logging.LoggedServerServiceCall.logged
import com.github.al.persistence.{PersistentEntityRegistrySugar, Retrying}
import com.github.al.roulette.player.api
import com.github.al.roulette.player.api._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class PlayerServiceImpl(override val entityRegistry: PersistentEntityRegistry, playerRepository: PlayerRepository)
                       (implicit val executionContext: ExecutionContext, scheduler: Scheduler)
  extends PlayerService
    with PersistentEntityRegistrySugar
    with Retrying
    with EventStreamLogging
    with LazyLogging {

  override def registerPlayer: ServiceCall[Player, PlayerId] = logged {
    ServerServiceCall { player =>
      val id = UUID.randomUUID()
      entityRef[PlayerEntity](id)
        .ask(CreatePlayer(PlayerState(player.playerName)))
        .map(_ => PlayerId(id))
    }
  }

  override def login: ServiceCall[PlayerCredentials, PlayerAccessToken] = {
    ServerServiceCall { credentials =>
      for {
        playerId <- retry(playerRepository.getPlayerIdByName(credentials.playerName), delay = 300 millis, timeout = 3 seconds)
        accessToken <- entityRef[PlayerEntity](playerId).ask(IssueAccessToken)
      } yield PlayerAccessToken(accessToken)
    }
  }

  override def getPlayer(id: UUID): ServiceCall[NotUsed, Player] = logged {
    ServerServiceCall { _: NotUsed =>
      entityRef[PlayerEntity](id).ask(GetPlayer).map {
        case Some(playerState) => Player(playerState.playerName)
        case None => throw NotFound(s"Player $id not found")
      }
    }
  }

  override def playerEvents: Topic[api.PlayerEvent] = TopicProducer.singleStreamWithOffset { offset =>
    entityRegistry.eventStream(PlayerEvent.Tag, offset)
      .filter(_.event.isInstanceOf[PlayerCreated])
      .mapAsync(1)(logEventStreamElementAsync).mapAsync(1)({
      case EventStreamElement(playerId, PlayerCreated(_), _offset) =>
        Future.successful(api.PlayerRegistered(playerId) -> _offset)
    })
  }
}
