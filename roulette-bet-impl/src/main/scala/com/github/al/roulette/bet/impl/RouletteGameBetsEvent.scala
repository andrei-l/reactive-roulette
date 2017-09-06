package com.github.al.roulette.bet.impl

import com.github.al.json.JsonFormats._
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

sealed trait RouletteGameBetsEvent extends AggregateEvent[RouletteGameBetsEvent] {
  override def aggregateTag: AggregateEventTagger[RouletteGameBetsEvent] = RouletteGameBetsEvent.Tag
}

object RouletteGameBetsEvent {
  val Tag: AggregateEventTag[RouletteGameBetsEvent] = AggregateEventTag[RouletteGameBetsEvent]
}

case object GameBettingStarted extends RouletteGameBetsEvent {
  implicit val format: Format[GameBettingStarted.type] = singletonFormat(GameBettingStarted)
}

case class BetPlaced(playerBet: PlayerBet) extends RouletteGameBetsEvent

case object BetPlaced {
  implicit val format: Format[BetPlaced] = Json.format
}

case class GameBettingFinished(playerBets: List[PlayerBet]) extends RouletteGameBetsEvent

case object GameBettingFinished {
  implicit val format: Format[GameBettingFinished] = Json.format
}




