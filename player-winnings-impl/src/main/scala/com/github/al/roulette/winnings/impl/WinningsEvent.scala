package com.github.al.roulette.winnings.impl

import com.github.al.json.JsonFormats._
import com.github.al.roulette.bet.api.PlayerBets
import com.github.al.roulette.winnings.api.PlayerWinning
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

sealed trait WinningsEvent extends AggregateEvent[WinningsEvent] {
  override def aggregateTag: AggregateEventTagger[WinningsEvent] = WinningsEvent.Tag
}

object WinningsEvent {
  val Tag: AggregateEventTag[WinningsEvent] = AggregateEventTag[WinningsEvent]
}

case class GameResultSaved(winningNumber: Int) extends WinningsEvent

case object GameResultSaved {
  implicit val format: Format[GameResultSaved] = Json.format
}

case class PlayersBetsSaved(playersBets: List[PlayerBets]) extends WinningsEvent

case object PlayersBetsSaved {
  implicit val format: Format[PlayersBetsSaved] = Json.format
}

case class WinningsCalculated(playerWinning: List[PlayerWinning]) extends WinningsEvent

case object WinningsCalculated {
  implicit val format: Format[WinningsCalculated] = Json.format
}
