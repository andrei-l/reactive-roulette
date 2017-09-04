package com.github.al.roulette.player.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class PlayerEntity extends PersistentEntity {
  override type Command = PlayerCommand
  override type Event = PlayerEvent
  override type State = Option[PlayerState]

  override def initialState: Option[PlayerState] = None

  override def behavior: Behavior = {
    case None => notCreated
    case Some(_) => getPlayerCommand
  }

  private lazy val notCreated: Actions = Actions()
    .onCommand[CreatePlayer, Done] {
    case (CreatePlayer(playerState), ctx, _) =>
      ctx.thenPersist(PlayerCreated(playerState))(_ => ctx.reply(Done))
  }.onEvent {
    case (PlayerCreated(playerState), _) =>
      Some(playerState)
  }.orElse(getPlayerCommand)

  private lazy val getPlayerCommand = Actions()
    .onReadOnlyCommand[GetPlayer.type, Option[PlayerState]] {
    case (GetPlayer, ctx, state) => ctx.reply(state)
  }

}
