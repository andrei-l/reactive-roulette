package com.github.al.authentication

import com.typesafe.config.ConfigFactory
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.libs.json.Json

import scala.util.Success

object JwtTokenUtil {
  private final val JwtKey = ConfigFactory.load().getString("jwt.key")
  private final val Algorithm = JwtAlgorithm.HS256

  def extractPayloadField(token: String, fieldName: String): Option[String] =
    JwtJson.decode(token, JwtKey, Seq(Algorithm)) match {
      case Success(jwtClaim: JwtClaim) => (Json.parse(jwtClaim.content) \ fieldName).asOpt[String]
      case _ => None
    }


  def createJwtToken(payloadField: String, payloadFieldValue: String): String = {
    val header = Json.obj("typ" -> "JWT", "alg" -> Algorithm.name).toString()
    val authClaim = JwtClaim(content = Json.obj(payloadField -> payloadFieldValue).toString()).issuedNow.expiresIn(600).toJson

    JwtJson.encode(header, authClaim, JwtKey, JwtAlgorithm.HS256)
  }

}
