package com.github.al.roulette.player.impl

import com.github.al.json.JsonFormats._
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

sealed trait PlayerEvent extends AggregateEvent[PlayerEvent] {
  override def aggregateTag: AggregateEventTagger[PlayerEvent] = PlayerEvent.Tag
}

object PlayerEvent {
  val Tag: AggregateEventTag[PlayerEvent] = AggregateEventTag[PlayerEvent]
}

case class PlayerCreated(playerState: PlayerState) extends PlayerEvent

case object PlayerCreated {
  implicit val format: Format[PlayerCreated] = Json.format
}
