package com.github.al.roulette.game.impl

import java.time.{Duration, Instant}

import com.github.al.json.JsonFormats._
import play.api.libs.json.{Format, Json}

case class GameState(gameName: String,
                     gameDuration: Duration,
                     gameStart: Option[Instant] = None,
                     gameEnd: Option[Instant] = None,
                     winningNumber: Option[Int] = None)

object GameState {
  implicit val format: Format[GameState] = Json.format
}
