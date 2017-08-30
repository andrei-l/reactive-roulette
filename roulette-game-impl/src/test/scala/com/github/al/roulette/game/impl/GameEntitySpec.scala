package com.github.al.roulette.game.impl

import java.time.Duration

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver.Reply
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

class GameEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues with MockitoSugar {
  private final val GameId = "7e595fac-830e-44f1-b73e-f8fd60594ace"
  private final val SampleGameState = GameState("Some new game", Duration.ofMinutes(30))

  private val system = ActorSystem("test", JsonSerializerRegistry.actorSystemSetupFor(GameSerializerRegistry))
  private val mockRouletteBallLander = mock[RouletteBallLander]


  "The game entity" should {
    "allow creating a game" in withDriver { driver =>
      val outcome = driver.run(CreateGame(SampleGameState))
      outcome.events should contain only GameCreated(SampleGameState)
      outcome.state should ===(Some(SampleGameState))
      outcome.sideEffects should contain only Reply(Done)
    }

    "allow creating and retrieving a game" in withDriver { driver =>
      val outcome = driver.run(CreateGame(SampleGameState), GetGame)
      outcome.events should contain only GameCreated(SampleGameState)
      outcome.state should ===(Some(SampleGameState))
      outcome.sideEffects should contain inOrderOnly (Reply(Done), Reply(Some(SampleGameState)))
    }
  }

  private def withDriver[T](block: PersistentEntityTestDriver[GameCommand, GameEvent, Option[GameState]] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new GameEntity(mockRouletteBallLander), GameId)
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }


  protected override def afterAll: Unit = TestKit.shutdownActorSystem(system)
}
