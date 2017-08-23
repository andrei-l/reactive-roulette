package com.github.al.roulette.game.impl

import java.time.Duration

import akka.Done
import com.github.al.json.JsonFormats._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

sealed trait GameCommand

case class CreateGame(gameState: GameState) extends GameCommand with ReplyType[Done]

case object GetGame extends GameCommand with ReplyType[Option[GameState]] {
  implicit val format: Format[GetGame.type] = singletonFormat(GetGame)
}

case class StartGame(duration: Duration) extends GameCommand

case object FinishGame extends GameCommand {
  implicit val format: Format[FinishGame.type] = singletonFormat(FinishGame)
}

object CreateGame {
  implicit val format: Format[CreateGame] = Json.format
}

object StartGame {
  implicit val format: Format[StartGame] = Json.format
}
