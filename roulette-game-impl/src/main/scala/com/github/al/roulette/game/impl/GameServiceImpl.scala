package com.github.al.roulette.game.impl

import java.util.UUID

import akka.persistence.query.Offset
import akka.{Done, NotUsed}
import com.github.al.roulette.game.api
import com.github.al.roulette.game.api.{Game, GameId, GameService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.concurrent.{ExecutionContext, Future}

class GameServiceImpl(registry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends GameService {
  override def createGame: ServiceCall[Game, GameId] = ServiceCall { game =>
    val id = UUID.randomUUID()
    entityRef(id)
      .ask(CreateGame(GameState(game.gameName, game.gameDuration)))
      .map(_ => GameId(id))
  }

  override def getGame(id: UUID): ServiceCall[NotUsed, Game] = ServiceCall { _ =>
    entityRef(id).ask(GetGame).map {
      case Some(gameState) => Game(gameState.gameName, gameState.gameDuration)
      case None => throw NotFound(s"Game $id not found");
    }
  }

  override def terminateGame(id: UUID): ServiceCall[NotUsed, Done] = ServiceCall {
    _ => Future.successful(Done)
  }

  override def gameEvents: Topic[api.GameEvent] = TopicProducer.singleStreamWithOffset { offset =>
    registry.eventStream(GameEvent.Tag, offset)
      .filter {
        _.event match {
          case _: GameCreated => true
          case _ => false
        }
      }.mapAsync(1)(convertEvent)
  }

  private def convertEvent: EventStreamElement[GameEvent] => Future[(api.GameEvent, Offset)] = {
    case EventStreamElement(itemId, GameCreated(GameState(_, gameDuration, _, _)), offset) =>
      Future.successful(api.GameCreated(UUID.fromString(itemId), gameDuration) -> offset)
  }

  private def entityRef(gameId: UUID) = entityRefString(gameId.toString)

  private def entityRefString(gameId: String) = registry.refFor[GameEntity](gameId)

}
