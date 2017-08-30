package com.github.al.roulette.game.impl

import java.util.UUID

case class PlayerBet(playerId: UUID, betNumber: Option[Int], betType: BetType, betAmount: BigDecimal)

sealed trait BetType

case object Number extends BetType

case object Odd extends BetType

case object Event extends BetType
