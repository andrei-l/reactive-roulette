package com.github.al.roulette.winnings

import com.github.al.roulette.winnings.impl._
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object WinningsSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[WinningsState],

    JsonSerializer[SaveGameResult],
    JsonSerializer[SavePlayersBets],

    JsonSerializer[GameResultSaved],
    JsonSerializer[PlayersBetsSaved],
    JsonSerializer[WinningsCalculated]
  )
}


