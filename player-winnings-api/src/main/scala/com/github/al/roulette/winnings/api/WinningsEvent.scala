package com.github.al.roulette.winnings.api

import java.util.UUID

import com.github.al.json.JsonFormats._
import julienrf.json.derived
import play.api.libs.json.{Format, Json, __}


sealed trait WinningsEvent {
  val gameId: UUID
}

case class WinningsCalculated(gameId: UUID, playerWinnings: List[PlayerWinning]) extends WinningsEvent

object WinningsCalculated {
  implicit val format: Format[WinningsCalculated] = Json.format
}

object WinningsEvent {
  implicit val format: Format[WinningsEvent] = derived.flat.oformat((__ \ "type").format[String])
}
