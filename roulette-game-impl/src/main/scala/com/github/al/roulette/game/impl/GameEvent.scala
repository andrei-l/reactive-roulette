package com.github.al.roulette.game.impl

import com.github.al.json.JsonFormats._
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

sealed trait GameEvent extends AggregateEvent[GameEvent] {
  override def aggregateTag: AggregateEventTagger[GameEvent] = GameEvent.Tag
}

object GameEvent {
  val Tag: AggregateEventTag[GameEvent] = AggregateEventTag[GameEvent]
}

case class GameCreated(gameState: GameState) extends GameEvent

object GameCreated {
  implicit val format: Format[GameCreated] = Json.format
}

case object GameStarted extends GameEvent {
  implicit val format: Format[GameStarted.type] = singletonFormat(GameStarted)
}

case object GameFinished extends GameEvent {
  implicit val format: Format[GameFinished.type] = singletonFormat(GameFinished)
}

case class GameResulted(winningNumber: Int) extends GameEvent

object GameResulted {
  implicit val format: Format[GameResulted] = Json.format
}
