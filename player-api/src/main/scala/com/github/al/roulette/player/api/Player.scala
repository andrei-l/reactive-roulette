package com.github.al.roulette.player.api

import java.util.UUID

import com.github.al.json.JsonFormats._
import play.api.libs.json.{Format, Json}

case class Player(playerName: String)

case class PlayerId(playerId: UUID)

case class PlayerCredentials(playerName: String)

case class PlayerAccessToken(token: String)


object Player {
  implicit val format: Format[Player] = Json.format
}

object PlayerId {
  implicit val format: Format[PlayerId] = Json.format
}

object PlayerCredentials {
  implicit val format: Format[PlayerCredentials] = Json.format
}

object PlayerAccessToken {
  implicit val format: Format[PlayerAccessToken] = Json.format
}
