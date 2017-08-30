package com.github.al.roulette.game.impl

import akka.Done
import com.github.al.json.JsonFormats._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

sealed trait GameCommand

case class CreateGame(gameState: GameState) extends GameCommand with ReplyType[Done]

object CreateGame {
  implicit val format: Format[CreateGame] = Json.format
}

case object GetGame extends GameCommand with ReplyType[Option[GameState]] {
  implicit val format: Format[GetGame.type] = singletonFormat(GetGame)
}

object StartGame extends GameCommand with ReplyType[Done] {
  implicit val format: Format[StartGame.type] = singletonFormat(StartGame)
}

case object FinishGame extends GameCommand with ReplyType[Done] {
  implicit val format: Format[FinishGame.type] = singletonFormat(FinishGame)
}

