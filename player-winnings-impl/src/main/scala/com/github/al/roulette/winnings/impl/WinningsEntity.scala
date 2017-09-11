package com.github.al.roulette.winnings.impl

import akka.Done
import com.github.al.roulette
import com.github.al.roulette.bet.api.{Bet, PlayerBets}
import com.github.al.roulette.winnings.api.PlayerWinning
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class WinningsEntity extends PersistentEntity {
  override type Command = WinningsCommand
  override type Event = WinningsEvent
  override type State = WinningsState

  override def initialState: WinningsState = WinningsState()

  override def behavior: Behavior =
    state => Actions()
      .onCommand[SavePlayersBets, Done] {
      case (SavePlayersBets(playersBets), ctx, _) =>
        val playersBetsSavedEvent = PlayersBetsSaved(playersBets)
        val events = appendWinningsCalculatedEventIfRequired(state.winningNumber, Some(playersBets), playersBetsSavedEvent)
        ctx.thenPersistAll(events: _*)(() => ctx.reply(Done))
    }.onCommand[SaveGameResult, Done] {
      case (SaveGameResult(winningNumber), ctx, _) =>
        val gameResultSavedEvent = GameResultSaved(winningNumber)
        val events = appendWinningsCalculatedEventIfRequired(Some(winningNumber), state.playersBets, gameResultSavedEvent)
        ctx.thenPersistAll(events: _*)(() => ctx.reply(Done))
    }.onEvent {
      case (PlayersBetsSaved(playersBets), _) =>
        state.copy(playersBets = Some(playersBets))
    }.onEvent {
      case (GameResultSaved(winningNumber), _) =>
        state.copy(winningNumber = Some(winningNumber))
    }.onEvent {
      case (WinningsCalculated(winnings), _) =>
        state.copy(playersWinnings = Some(winnings))
    }

  private def appendWinningsCalculatedEventIfRequired(winningNumberOption: Option[Int],
                                                      playersBetsOption: Option[List[PlayerBets]],
                                                      winningsEvent: WinningsEvent): List[WinningsEvent] = {
    val winningsCalculatedEventOption = for {
      winningNumber <- winningNumberOption
      playersBets <- playersBetsOption
      winningsCalculatedEvent = WinningsCalculated(calculateWinnings(winningNumber, playersBets))
    } yield winningsCalculatedEvent

    winningsCalculatedEventOption.foldRight[List[WinningsEvent]](Nil)(_ :: _) ::: List(winningsEvent)
  }

  private def calculateWinnings(winningNumber: Int, playersBets: List[PlayerBets]): List[PlayerWinning] =
    playersBets.map { case PlayerBets(playerId, bets) =>
      PlayerWinning(playerId, bets.map(getWinnings(winningNumber, _)).sum)
    }

  private def getWinnings(winningNumber: Int, bet: Bet): BigDecimal = {
    bet match {
      case Bet(Some(betNumber), roulette.bet.api.Number, betAmount) if betNumber == winningNumber => betAmount * 36
      case Bet(None, roulette.bet.api.Even, betAmount) if winningNumber % 2 == 0 => betAmount * 2
      case Bet(None, roulette.bet.api.Odd, betAmount) if winningNumber % 2 != 0 => betAmount * 2
      case _ => 0
    }
  }
}
