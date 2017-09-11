package com.github.al.roulette.winnings.api

import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service}

trait WinningsService extends Service {
  def winningsEvents: Topic[WinningsEvent]


  final override def descriptor: Descriptor = {
    import Service._

    named("player-winnings").withTopics(
      topic(WinningsService.WinningsEventTopicName, this.winningsEvents)
    ).withAutoAcl(true)
  }
}

object WinningsService {
  final val WinningsEventTopicName = "winnings-WinningsEvent"
}
