package com.github.al.roulette.game.api

import java.time.Duration
import java.util.UUID

import com.github.al.json.JsonFormats._
import julienrf.json.derived
import play.api.libs.json.{Format, Json, __}


sealed trait GameEvent {
  val gameId: UUID
}

case class GameCreated(gameId: UUID, gameDuration: Duration) extends GameEvent


object GameCreated {
  implicit val format: Format[GameCreated] = Json.format
}

object GameEvent {
  implicit val format: Format[GameEvent] = derived.flat.oformat((__ \ "type").format[String])
}
