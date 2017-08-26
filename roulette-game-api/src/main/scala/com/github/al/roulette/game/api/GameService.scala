package com.github.al.roulette.game.api

import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait GameService extends Service {
  final val GameEventTopicName = "game-GameEvent"

  def createGame: ServiceCall[Game, GameId]

  def getGame(id: UUID): ServiceCall[NotUsed, Game]

  def terminateGame(id: UUID): ServiceCall[NotUsed, Done]

  def gameEvents: Topic[GameEvent]

  final override def descriptor: Descriptor = {
    import Service._

    named("game").withCalls(
      pathCall("/api/recurring-game", createGame),
      pathCall("/api/recurring-game/:id", getGame _),
      restCall(Method.POST, "/api/recurring-game/:id/terminate", terminateGame _)
    ).withTopics(
      topic(GameEventTopicName, this.gameEvents)
    ).withAutoAcl(true)
  }
}

object GameService {
  final val GameEventTopicName = "game-GameEvent"
}

