package com.github.al.roulette.player.impl

import akka.Done
import com.github.al.authentication.JwtTokenUtil
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class PlayerEntity extends PersistentEntity {
  override type Command = PlayerCommand
  override type Event = PlayerEvent
  override type State = Option[PlayerState]

  override def initialState: Option[PlayerState] = None

  override def behavior: Behavior = {
    case None => notCreated
    case Some(playerState) => created(playerState)
  }

  private lazy val notCreated: Actions = Actions()
    .onCommand[CreatePlayer, Done] {
    case (CreatePlayer(playerState), ctx, _) =>
      ctx.thenPersist(PlayerCreated(playerState))(_ => ctx.reply(Done))
  }.onEvent {
    case (PlayerCreated(playerState), _) =>
      Some(playerState)
  }.orElse(getPlayerCommand)

  private def created(playerState: PlayerState): Actions = Actions()
    .onCommand[IssueAccessToken.type, String] {
    case (IssueAccessToken, ctx, _) =>
      val accessToken = JwtTokenUtil.createJwtToken("playerId", entityId)
      ctx.thenPersist(AccessTokenIssued(accessToken))(_ => ctx.reply(accessToken))
  }.onEvent {
    case (AccessTokenIssued(accessToken), _) =>
      Some(playerState.copy(accessTokens = accessToken :: playerState.accessTokens))
  }.orElse(getPlayerCommand)

  private lazy val getPlayerCommand = Actions()
    .onReadOnlyCommand[GetPlayer.type, Option[PlayerState]] {
    case (GetPlayer, ctx, state) => ctx.reply(state)
  }
}
