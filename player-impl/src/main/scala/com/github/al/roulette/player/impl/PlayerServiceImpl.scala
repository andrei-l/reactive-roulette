package com.github.al.roulette.player.impl

import java.util.UUID

import akka.NotUsed
import akka.actor.Scheduler
import com.github.al.persistence.{PersistentEntityRegistrySugar, Retrying}
import com.github.al.roulette.player.api
import com.github.al.roulette.player.api._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class PlayerServiceImpl(override val entityRegistry: PersistentEntityRegistry, playerRepository: PlayerRepository)
                       (implicit val executionContext: ExecutionContext, scheduler: Scheduler)
  extends PlayerService
    with PersistentEntityRegistrySugar
    with Retrying {

  override def registerPlayer: ServiceCall[Player, PlayerId] = ServiceCall { player =>
    val id = UUID.randomUUID()
    entityRef[PlayerEntity](id)
      .ask(CreatePlayer(PlayerState(player.playerName)))
      .map(_ => PlayerId(id))
  }

  override def login: ServiceCall[PlayerCredentials, PlayerAccessToken] = ServiceCall { credentials =>
    for {
      playerId <- retry(playerRepository.getPlayerIdByName(credentials.playerName), delay = 300 millis, timeout = 3 seconds)
      accessToken <- entityRef[PlayerEntity](playerId).ask(IssueAccessToken)
    } yield PlayerAccessToken(accessToken)
  }

  override def getPlayer(id: UUID): ServiceCall[NotUsed, Player] = ServiceCall { _ =>
    entityRef[PlayerEntity](id).ask(GetPlayer).map {
      case Some(playerState) => Player(playerState.playerName)
      case None => throw NotFound(s"Player $id not found")
    }
  }

  override def playerEvents: Topic[api.PlayerEvent] = TopicProducer.singleStreamWithOffset { offset =>
    entityRegistry.eventStream(PlayerEvent.Tag, offset)
      .filter(_.event.isInstanceOf[PlayerCreated])
      .mapAsync(1)({
        case EventStreamElement(playerId, PlayerCreated(_), _offset) =>
          Future.successful(api.PlayerRegistered(playerId) -> _offset)
      })
  }
}
