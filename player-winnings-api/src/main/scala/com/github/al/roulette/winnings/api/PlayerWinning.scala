package com.github.al.roulette.winnings.api

import java.util.UUID

import play.api.libs.json.{Format, Json}

case class PlayerWinning(playerId: UUID, winning: BigDecimal)

object PlayerWinning {
  implicit val format: Format[PlayerWinning] = Json.format
}

