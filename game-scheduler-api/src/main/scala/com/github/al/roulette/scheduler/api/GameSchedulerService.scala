package com.github.al.roulette.scheduler.api

import com.github.al.roulette.scheduler.api.GameSchedulerService.ScheduledGameEventTopicName
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service}

trait GameSchedulerService extends Service {
  def scheduledEvents: Topic[ScheduledGameEvent]

  final override def descriptor: Descriptor = {
    import Service._

    named("game-scheduler").withTopics(
      topic(ScheduledGameEventTopicName, this.scheduledEvents)
    ).withAutoAcl(true)
  }
}

object GameSchedulerService {
  final val ScheduledGameEventTopicName = "scheduler-ScheduledEvent"
}
