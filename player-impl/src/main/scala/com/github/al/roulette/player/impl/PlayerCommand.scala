package com.github.al.roulette.player.impl

import akka.Done
import com.github.al.json.JsonFormats._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

sealed trait PlayerCommand

case class CreatePlayer(playerState: PlayerState) extends PlayerCommand with ReplyType[Done]

object CreatePlayer {
  implicit val format: Format[CreatePlayer] = Json.format
}

case object GetPlayer extends PlayerCommand with ReplyType[Option[PlayerState]] {
  implicit val format: Format[GetPlayer.type] = singletonFormat(GetPlayer)
}
