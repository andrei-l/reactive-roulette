package com.github.al.roulette.scheduler.api

import java.util.UUID

import com.github.al.json.JsonFormats._
import julienrf.json.derived
import play.api.libs.json.{Format, Json, __}


sealed trait ScheduledGameEvent {
  val gameId: UUID
}

case class GameStarted(gameId: UUID) extends ScheduledGameEvent

object GameStarted {
  implicit val format: Format[GameStarted] = Json.format
}

case class GameFinished(gameId: UUID) extends ScheduledGameEvent

object GameFinished {
  implicit val format: Format[GameFinished] = Json.format
}

object ScheduledGameEvent {
  implicit val format: Format[ScheduledGameEvent] = derived.flat.oformat((__ \ "type").format[String])
}
