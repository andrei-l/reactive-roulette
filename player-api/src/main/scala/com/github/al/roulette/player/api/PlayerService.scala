package com.github.al.roulette.player.api

import java.util.UUID

import akka.NotUsed
import com.github.al.roulette.player.api.PlayerService.PlayerEventTopicName
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait PlayerService extends Service {
  def registerPlayer: ServiceCall[Player, PlayerId]

  def login: ServiceCall[PlayerCredentials, PlayerAccessToken]

  def getPlayer(id: UUID): ServiceCall[NotUsed, Player]

  def playerEvents: Topic[PlayerEvent]

  final override def descriptor: Descriptor = {
    import Service._

    named("player").withCalls(
      pathCall("/api/player", registerPlayer),
      pathCall("/api/login", login),
      pathCall("/api/player/:id", getPlayer _)
    ).withTopics(
      topic(PlayerEventTopicName, this.playerEvents)
    ).withAutoAcl(true)
  }
}

object PlayerService {
  final val PlayerEventTopicName = "player-PlayerEvent"
}
