package com.github.al.roulette.bet.api

import java.util.UUID

import julienrf.json.derived
import play.api.libs.json.{Format, Json, __}

sealed trait RouletteBetsEvent {
  def gameId: UUID
}

case class AllGameBetsProclaimed(gameId: UUID, playersBets: List[PlayerBets]) extends RouletteBetsEvent

object AllGameBetsProclaimed {
  implicit val format: Format[AllGameBetsProclaimed] = Json.format
}

object RouletteBetsEvent {
  implicit val format: Format[RouletteBetsEvent] = derived.flat.oformat((__ \ "type").format[String])
}
