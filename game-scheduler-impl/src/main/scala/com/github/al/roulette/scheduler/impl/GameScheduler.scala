package com.github.al.roulette.scheduler.impl

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import com.github.al.roulette.game.api.{GameCreated, GameEvent, GameService}
import com.github.al.roulette.scheduler.api.{GameFinished, GameStarted, ScheduledGameEvent}
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}

import scala.concurrent.duration.{DurationDouble, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps


class GameScheduler(gameService: GameService,
                    system: ActorSystem,
                    pubSub: PubSubRegistry)(implicit ec: ExecutionContext) {
  private val gameSchedulerEventsTopic = pubSub.refFor(TopicId[ScheduledGameEvent])

  gameService.gameEvents.subscribe.atLeastOnce(Flow[GameEvent].mapAsync(1) {
    case GameCreated(gameId, gameDuration) => runGame(gameId, gameDuration.toMillis millis)
    case _ => Future.successful(Done)
  })

  private def runGame(gameId: UUID, gameDuration: FiniteDuration) = {
    gameSchedulerEventsTopic.publish(GameStarted(gameId))
    system.scheduler.scheduleOnce(gameDuration)(finishGame(gameId))
    Future.successful(Done)
  }

  private def finishGame(gameId: UUID) = {
    gameSchedulerEventsTopic.publish(GameFinished(gameId))
  }
}
