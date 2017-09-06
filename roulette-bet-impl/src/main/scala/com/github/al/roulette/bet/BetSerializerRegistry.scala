package com.github.al.roulette.bet

import com.github.al.roulette.bet.impl.{GameBettingStarted, _}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object BetSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[RouletteGameBetsState],

    JsonSerializer[StartGameBetting.type],
    JsonSerializer[PlaceBet],
    JsonSerializer[FinishGameBetting.type],

    JsonSerializer[GameBettingStarted.type],
    JsonSerializer[BetPlaced],
    JsonSerializer[GameBettingFinished]
  )
}


