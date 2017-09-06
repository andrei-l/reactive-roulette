package com.github.al.roulette.bet.api

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait BetService extends Service {
  def placeBet(gameId: UUID): ServiceCall[Bet, NotUsed]

  final override def descriptor: Descriptor = {
    import Service._

    named("bet").withCalls(
      pathCall("/api/roulette-game-bet/:gameId/bet", placeBet _)
    ).withAutoAcl(true)
  }
}

object BetService {
  final val BetEventTopicName = "bet-BetEvent"
}
