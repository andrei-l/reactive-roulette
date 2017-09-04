package com.github.al.roulette.player.api

import java.util.UUID

import com.github.al.json.JsonFormats._
import play.api.libs.json.{Format, Json}

case class Player(playerName: String)

case class PlayerId(playerId: UUID)

object Player {
  implicit val format: Format[Player] = Json.format
}

object PlayerId {
  implicit val format: Format[PlayerId] = Json.format
}
