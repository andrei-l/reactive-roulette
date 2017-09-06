package com.github.al.roulette.player

import com.github.al.roulette.player.impl._
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object PlayerSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[PlayerState],

    JsonSerializer[CreatePlayer],
    JsonSerializer[IssueAccessToken.type],
    JsonSerializer[GetPlayer.type],

    JsonSerializer[PlayerCreated],
    JsonSerializer[AccessTokenIssued]
  )
}


