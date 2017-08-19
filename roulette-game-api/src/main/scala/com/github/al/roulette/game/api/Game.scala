package com.github.al.roulette.game.api

import java.time.Duration
import java.util.UUID

import com.github.al.json.JsonFormats._
import play.api.libs.json.{Format, Json}

case class Game(id: Option[UUID], gameName: String, gameDuration: Duration)

object Game {
  implicit val format: Format[Game] = Json.format
}
