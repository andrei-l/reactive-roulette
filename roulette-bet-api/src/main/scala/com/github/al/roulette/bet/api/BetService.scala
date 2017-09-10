package com.github.al.roulette.bet.api

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait BetService extends Service {
  def placeBet(gameId: UUID): ServiceCall[Bet, NotUsed]

  def rouletteBetsEvents: Topic[RouletteBetsEvent]

  final override def descriptor: Descriptor = {
    import Service._

    named("roulette-bets").withCalls(
      pathCall("/api/roulette-game-bet/:gameId/bet", placeBet _)
    ).withTopics(
      topic(BetService.BetEventTopicName, this.rouletteBetsEvents)
    ).withAutoAcl(true)
  }

}

object BetService {
  final val BetEventTopicName = "bet-RouletteBetsEvent"
}
