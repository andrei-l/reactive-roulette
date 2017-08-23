package com.github.al.roulette.game.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class GameEntity extends PersistentEntity {
  override type Command = GameCommand
  override type Event = GameEvent
  override type State = Option[GameState]

  override def initialState: Option[GameState] = None

  override def behavior: Behavior = {
    case None => notCreated
    case Some(gameState) => created(gameState)
  }

  private lazy val notCreated: Actions = {
    Actions().onCommand[CreateGame, Done] {
      case (CreateGame(gameState), ctx, _) =>
        ctx.thenPersist(GameCreated(gameState))(_ => ctx.reply(Done))
    }.onEvent {
      case (GameCreated(gameState), _) => Some(gameState)
    }.orElse(getGameCommand)
  }

  private def created(gameState: GameState) = {
    Actions()
      .orElse(getGameCommand)
  }

  private lazy val getGameCommand = Actions().onReadOnlyCommand[GetGame.type, Option[GameState]] {
    case (GetGame, ctx, state) => ctx.reply(state)
  }

}
