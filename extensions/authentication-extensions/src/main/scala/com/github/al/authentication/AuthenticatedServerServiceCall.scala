package com.github.al.authentication

import java.util.UUID

import com.lightbend.lagom.scaladsl.api.transport.{Forbidden, RequestHeader}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import play.mvc.Http.HeaderNames

import scala.concurrent.Future
import scala.util.Try

object AuthenticatedServerServiceCall {
  private final val JwtHeaderRegex = "(Bearer) (.*)".r

  def authenticated[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] =
    ServerServiceCall.composeAsync { requestHeader =>
      extractToken(requestHeader).flatMap(extractPlayerId).flatMap(playerId => Try(UUID.fromString(playerId)).toOption) match {
        case Some(playerId) => Future.successful(serviceCall(playerId))
        case None => throw Forbidden("Player must be authenticated to access this service call")
      }
    }

  private def extractToken(requestHeader: RequestHeader) =
    requestHeader.getHeader(HeaderNames.AUTHORIZATION).map({ case JwtHeaderRegex(_, token) => token })

  private def extractPlayerId(token: String): Option[String] =
    JwtTokenUtil.extractPayloadField(token, "playerId")
}
