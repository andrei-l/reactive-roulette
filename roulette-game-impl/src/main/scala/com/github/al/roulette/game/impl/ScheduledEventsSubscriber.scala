package com.github.al.roulette.game.impl

import akka.stream.scaladsl.Flow
import com.github.al.persistence.PersistentEntityRegistrySugar
import com.github.al.roulette.scheduler
import com.github.al.roulette.scheduler.api.{GameSchedulerService, ScheduledGameEvent}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext

class ScheduledEventsSubscriber(gameSchedulerService: GameSchedulerService,
                                override val entityRegistry: PersistentEntityRegistry)(implicit executionContext: ExecutionContext)
  extends PersistentEntityRegistrySugar {

  gameSchedulerService.scheduledEvents.subscribe.atLeastOnce(Flow[ScheduledGameEvent].mapAsync(1) {
    e =>
      entityRef[GameEntity](e.gameId).ask(e match {
        case _: scheduler.api.GameStarted => StartGame
        case _: scheduler.api.GameFinished => FinishGame
      })
  })
}
