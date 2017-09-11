package com.github.al.roulette.winnings.impl

import akka.Done
import com.github.al.json.JsonFormats._
import com.github.al.roulette.bet.api.PlayerBets
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

sealed trait WinningsCommand

case class SaveGameResult(winningNumber: Int) extends WinningsCommand with ReplyType[Done]

case object SaveGameResult extends WinningsCommand with ReplyType[Done] {
  implicit val format: Format[SaveGameResult] = Json.format
}

case class SavePlayersBets(playersBets: List[PlayerBets]) extends WinningsCommand with ReplyType[Done]

object SavePlayersBets {
  implicit val format: Format[SavePlayersBets] = Json.format
}



