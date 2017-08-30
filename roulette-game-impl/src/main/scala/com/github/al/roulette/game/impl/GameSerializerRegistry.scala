package com.github.al.roulette.game.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object GameSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[GameState],

    JsonSerializer[CreateGame],
    JsonSerializer[FinishGame.type],
    JsonSerializer[GetGame.type],

    JsonSerializer[GameCreated],
    JsonSerializer[GameStarted.type],
    JsonSerializer[GameFinished.type],
    JsonSerializer[GameResulted]
  )
}


