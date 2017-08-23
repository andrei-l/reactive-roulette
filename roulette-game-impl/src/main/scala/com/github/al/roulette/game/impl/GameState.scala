package com.github.al.roulette.game.impl

import java.time.{Duration, Instant}

import play.api.libs.json.{Format, Json}
import com.github.al.json.JsonFormats._

case class GameState(gameName: String, gameDuration: Duration, gameStart: Option[Instant] = None, gameEnd: Option[Instant] = None)

object GameState {
  implicit val format: Format[GameState] = Json.format
}
