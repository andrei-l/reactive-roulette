package com.github.al.roulette.bet.api

import java.util.UUID

import com.github.al.json.JsonFormats._
import julienrf.json.derived
import play.api.libs.json.{Format, Json, __}


case class PlayerBets(playerId: UUID, bets: List[Bet])

object PlayerBets {
  implicit val format: Format[PlayerBets] = Json.format
}

case class Bet(betNumber: Option[Int], betType: BetType, betAmount: BigDecimal)

object Bet {
  implicit val format: Format[Bet] = Json.format
}

sealed trait BetType

case object Number extends BetType {
  implicit val format: Format[Number.type] = singletonFormat(Number)
}

case object Odd extends BetType {
  implicit val format: Format[Odd.type] = singletonFormat(Odd)
}

case object Event extends BetType {
  implicit val format: Format[Event.type] = singletonFormat(Event)
}

object BetType {
  implicit val format: Format[BetType] = derived.flat.oformat((__ \ "type").format[String])
}

