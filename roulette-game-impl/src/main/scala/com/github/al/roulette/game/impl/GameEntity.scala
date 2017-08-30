package com.github.al.roulette.game.impl

import java.time.Instant.now

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class GameEntity(rouletteBallLander: RouletteBallLander) extends PersistentEntity {
  override type Command = GameCommand
  override type Event = GameEvent
  override type State = Option[GameState]

  override def initialState: Option[GameState] = None

  override def behavior: Behavior = {
    case None => notCreated
    case Some(gameState@GameState(_, _, None, None, None)) => created(gameState)
    case Some(gameState@GameState(_, _, Some(_), None, None)) => started(gameState)
    case Some(gameState@GameState(_, _, Some(_), Some(_), Some(_))) => finished(gameState)
    case _ => getGameCommand
  }

  private lazy val notCreated: Actions = Actions()
    .onCommand[CreateGame, Done] {
    case (CreateGame(gameState), ctx, _) =>
      ctx.thenPersist(GameCreated(gameState))(_ => ctx.reply(Done))
  }.onEvent {
    case (GameCreated(gameState), _) =>
      Some(gameState)
  }.orElse(getGameCommand)

  private def created(gameState: GameState) = Actions()
    .onCommand[StartGame.type, Done] {
    case (StartGame, ctx, _) =>
      ctx.thenPersist(GameStarted)(_ => ctx.reply(Done))
  }.onEvent {
    case (GameStarted, currentState) =>
      currentState.map(_.copy(gameStart = Some(now())))
  }.orElse(getGameCommand)

  private def started(gameState: GameState) = Actions()
    .onCommand[FinishGame.type, Done] {
    case (FinishGame, ctx, _) =>
      ctx.thenPersistAll(GameFinished, GameResulted(rouletteBallLander.landBall()))(() => ctx.reply(Done))
  }.onEvent {
    case (GameFinished, currentState) =>
      currentState.map(_.copy(gameEnd = Some(now())))
    case (GameResulted(winningNumber), currentState) =>
      currentState.map(_.copy(winningNumber = Some(winningNumber)))
  }.orElse(getGameCommand)


  private def finished(gameState: GameState) = {
    Actions()
      .orElse(getGameCommand)
  }

  private lazy val getGameCommand = Actions()
    .onReadOnlyCommand[GetGame.type, Option[GameState]] {
    case (GetGame, ctx, state) => ctx.reply(state)
  }

}
