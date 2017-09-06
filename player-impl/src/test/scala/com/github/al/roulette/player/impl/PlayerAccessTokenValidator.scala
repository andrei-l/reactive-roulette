package com.github.al.roulette.player.impl

import com.typesafe.config.ConfigFactory
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.libs.json.Json

import scala.util.Success

private[impl] object PlayerAccessTokenValidator {
  private final val JwtKey = ConfigFactory.load().getString("jwt.key")

  def isValidAccessToken(token: String, playerId: String): Boolean =
    JwtJson.decode(token, JwtKey, Seq(JwtAlgorithm.HS256)) match {
      case Success(jwtClaim: JwtClaim) => (Json.parse(jwtClaim.content) \ "playerId").asOpt[String].contains(playerId)
      case _ => false
    }
}
