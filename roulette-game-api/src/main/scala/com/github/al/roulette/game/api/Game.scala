package com.github.al.roulette.game.api

import java.time.Duration
import java.util.UUID

import com.github.al.json.JsonFormats._
import play.api.libs.json.{Format, Json}

case class Game(gameName: String, gameDuration: Duration)

case class GameId(gameId: UUID)

object Game {
  implicit val format: Format[Game] = Json.format
}

object GameId {
  implicit val format: Format[GameId] = Json.format
}
