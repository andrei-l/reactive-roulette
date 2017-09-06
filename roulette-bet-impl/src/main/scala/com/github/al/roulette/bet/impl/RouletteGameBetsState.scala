package com.github.al.roulette.bet.impl

import java.util.UUID

import com.github.al.json.JsonFormats._
import com.github.al.roulette.bet.api.Bet
import play.api.libs.json.{Format, Json}

case class RouletteGameBetsState(bets: List[PlayerBet] = Nil, gameFinished: Boolean = false)

case class PlayerBet(playerId: UUID, bet: Bet)

object RouletteGameBetsState {
  implicit val format: Format[RouletteGameBetsState] = Json.format
}

object PlayerBet {
  implicit val format: Format[PlayerBet] = Json.format
}

