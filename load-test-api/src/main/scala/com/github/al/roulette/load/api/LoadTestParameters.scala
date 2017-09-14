package com.github.al.roulette.load.api

import play.api.libs.json.{Format, Json}

case class LoadTestParameters(numberOfConcurrentGames: Int,
                              numberOfPlayers: Int,
                              numberOfGamesToPlay: Int)

object LoadTestParameters {
  implicit val format: Format[LoadTestParameters] = Json.format
}

