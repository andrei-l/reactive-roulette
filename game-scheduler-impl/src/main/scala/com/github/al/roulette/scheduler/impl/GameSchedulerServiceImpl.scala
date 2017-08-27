package com.github.al.roulette.scheduler.impl

import com.github.al.roulette.scheduler.api.{GameFinished, GameSchedulerService, GameStarted, ScheduledEvent}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}

import scala.concurrent.Future

class GameSchedulerServiceImpl(pubSubRegistry: PubSubRegistry) extends GameSchedulerService {
  private val internalGameSchedulerEventsTopic = pubSubRegistry.refFor(TopicId[ScheduledEvent])

  override def scheduledEvents: Topic[ScheduledEvent] = TopicProducer.singleStreamWithOffset { offset =>
    internalGameSchedulerEventsTopic.subscriber.filter {
      case _: GameStarted | _: GameFinished => true
      case _ => false
    }.mapAsync(1)(event => Future.successful(event -> offset))
  }
}
