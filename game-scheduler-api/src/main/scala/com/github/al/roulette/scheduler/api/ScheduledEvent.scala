package com.github.al.roulette.scheduler.api

import java.util.UUID

import com.github.al.json.JsonFormats._
import julienrf.json.derived
import play.api.libs.json.{Format, Json, __}


sealed trait ScheduledEvent {
  val gameId: UUID
}

case class GameStarted(gameId: UUID) extends ScheduledEvent

object GameStarted {
  implicit val format: Format[GameStarted] = Json.format
}

case class GameFinished(gameId: UUID) extends ScheduledEvent

object GameFinished {
  implicit val format: Format[GameFinished] = Json.format
}

object ScheduledEvent {
  implicit val format: Format[ScheduledEvent] = derived.flat.oformat((__ \ "type").format[String])
}
