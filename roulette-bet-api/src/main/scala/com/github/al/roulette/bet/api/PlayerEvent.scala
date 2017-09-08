package com.github.al.roulette.bet.api

import java.util.UUID

import julienrf.json.derived
import play.api.libs.json.{Format, Json, __}

sealed trait PlayerEvent {
  val playerId: UUID
}

case class PlayerRegistered(playerId: UUID) extends PlayerEvent

object PlayerRegistered {
  implicit val format: Format[PlayerRegistered] = Json.format
}

object PlayerEvent {
  implicit val format: Format[PlayerEvent] = derived.flat.oformat((__ \ "type").format[String])
}
