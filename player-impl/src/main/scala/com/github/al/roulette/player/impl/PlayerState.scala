package com.github.al.roulette.player.impl

import com.github.al.json.JsonFormats._
import play.api.libs.json.{Format, Json}

case class PlayerState(playerName: String)

object PlayerState {
  implicit val format: Format[PlayerState] = Json.format
}
