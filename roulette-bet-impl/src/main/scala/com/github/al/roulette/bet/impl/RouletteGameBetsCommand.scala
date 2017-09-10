package com.github.al.roulette.bet.impl

import java.util.UUID

import akka.Done
import com.github.al.json.JsonFormats._
import com.github.al.roulette.bet.api.Bet
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

sealed trait RouletteGameBetsCommand

case object StartGameBetting extends RouletteGameBetsCommand with ReplyType[Done] {
  implicit val format: Format[StartGameBetting.type] = singletonFormat(StartGameBetting)
}

case class PlaceBet(playerId: UUID, bet: Bet) extends RouletteGameBetsCommand with ReplyType[Done]

object PlaceBet {
  implicit val format: Format[PlaceBet] = Json.format
}

case object FinishGameBetting extends RouletteGameBetsCommand with ReplyType[Done] {
  implicit val format: Format[FinishGameBetting.type] = singletonFormat(FinishGameBetting)
}


