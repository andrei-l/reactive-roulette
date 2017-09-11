package com.github.al.roulette.winnings.impl

import com.github.al.json.JsonFormats._
import com.github.al.roulette.bet.api.PlayerBets
import com.github.al.roulette.winnings.api.PlayerWinning
import play.api.libs.json.{Format, Json}

case class WinningsState(winningNumber: Option[Int] = None,
                         playersBets: Option[List[PlayerBets]] = None,
                         playersWinnings: Option[List[PlayerWinning]] = None)

object WinningsState {
  implicit val format: Format[WinningsState] = Json.format
}
