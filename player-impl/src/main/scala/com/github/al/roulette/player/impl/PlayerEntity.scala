package com.github.al.roulette.player.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.typesafe.config.ConfigFactory
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.libs.json.Json

class PlayerEntity extends PersistentEntity {
  override type Command = PlayerCommand
  override type Event = PlayerEvent
  override type State = Option[PlayerState]

  private final val JwtKey = ConfigFactory.load().getString("jwt.key")

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
      val accessToken = createJwtToken(entityId)
      ctx.thenPersist(AccessTokenIssued(accessToken))(_ => ctx.reply(accessToken))
  }.onEvent {
    case (AccessTokenIssued(accessToken), _) =>
      Some(playerState.copy(accessTokens = accessToken :: playerState.accessTokens))
  }.orElse(getPlayerCommand)

  private lazy val getPlayerCommand = Actions()
    .onReadOnlyCommand[GetPlayer.type, Option[PlayerState]] {
    case (GetPlayer, ctx, state) => ctx.reply(state)
  }

  private def createJwtToken(playerId: String): String = {
    val header = Json.obj("typ" -> "JWT", "alg" -> "HS256").toString()
    val authClaim = JwtClaim(content = Json.obj("playerId" -> playerId).toString()).issuedNow.expiresIn(600).toJson

    JwtJson.encode(header, authClaim, JwtKey, JwtAlgorithm.HS256)
  }

}
