package com.github.al.roulette.scheduler.api

import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service}

trait GameSchedulerService extends Service {
  def scheduledEvents: Topic[ScheduledEvent]

  final override def descriptor: Descriptor = {
    import Service._

    named("game-scheduler").withTopics(
      topic("scheduler-ScheduledEvent", this.scheduledEvents)
    ).withAutoAcl(true)
  }
}
