package com.github.al.roulette.bet.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class RouletteGameBetsEntity extends PersistentEntity {
  override type Command = RouletteGameBetsCommand
  override type Event = RouletteGameBetsEvent
  override type State = Option[RouletteGameBetsState]

  override def initialState: Option[RouletteGameBetsState] = None

  override def behavior: Behavior = {
    case None => notRegisteredGame
    case Some(state@RouletteGameBetsState(_, false)) => bettingStarted(state)
    case Some(state@RouletteGameBetsState(_, true)) => bettingFinished(state)
  }

  private lazy val notRegisteredGame: Actions = Actions()
    .onCommand[StartGameBetting.type, Done] {
    case (StartGameBetting, ctx, _) =>
      ctx.thenPersist(GameBettingStarted)(_ => ctx.reply(Done))
  }.onEvent {
    case (GameBettingStarted, _) =>
      Some(RouletteGameBetsState())
  }

  private def bettingStarted(rouletteGameBetsState: RouletteGameBetsState): Actions = Actions()
    .onCommand[PlaceBet, Done] {
    case (PlaceBet(playerId, bet), ctx, _) =>
      ctx.thenPersist(BetPlaced(PlayerBet(playerId, bet)))(_ => ctx.reply(Done))
  }.onCommand[FinishGameBetting.type, Done] {
    case (FinishGameBetting, ctx, _) =>
      ctx.thenPersist(GameBettingFinished(rouletteGameBetsState.bets))(_ => ctx.reply(Done))
  }.onEvent {
    case (BetPlaced(playerBet), _) =>
      Some(rouletteGameBetsState.copy(bets = playerBet :: rouletteGameBetsState.bets))
  }.onEvent {
    case (GameBettingFinished(_), _) =>
      Some(rouletteGameBetsState.copy(gameFinished = true))
  }

  private def bettingFinished(rouletteGameBetsState: RouletteGameBetsState): Actions = Actions()
}
