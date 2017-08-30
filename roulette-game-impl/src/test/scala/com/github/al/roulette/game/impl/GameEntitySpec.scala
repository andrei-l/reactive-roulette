package com.github.al.roulette.game.impl

import java.time.Duration

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver.Reply
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

class GameEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues with MockitoSugar {
  private final val GameId = "7e595fac-830e-44f1-b73e-f8fd60594ace"
  private final val GameName = "Some new game"
  private final val GameDuration = Duration.ofMinutes(30)
  private final val SampleGameState = GameState(GameName, GameDuration)

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
      outcome.sideEffects should contain theSameElementsInOrderAs Seq(Reply(Done), Reply(Some(SampleGameState)))
    }

    "allow creating and starting a game" in withDriver { driver =>
      val outcome = driver.run(CreateGame(SampleGameState), StartGame)
      outcome.events should contain theSameElementsInOrderAs Seq(GameCreated(SampleGameState), GameStarted)
      outcome.state should matchPattern { case Some(GameState(GameName, GameDuration, Some(_), None, None)) => }
      outcome.sideEffects should contain theSameElementsInOrderAs Seq(Reply(Done), Reply(Done))
    }

    "allow creating, starting and finishing a game" in withDriver { driver =>
      when(mockRouletteBallLander.landBall()).thenReturn(11)
      val outcome = driver.run(CreateGame(SampleGameState), StartGame, FinishGame)
      outcome.events should contain theSameElementsInOrderAs Seq(GameCreated(SampleGameState), GameStarted, GameFinished, GameResulted(11))
      outcome.state should matchPattern { case Some(GameState(GameName, GameDuration, Some(_), Some(_), Some(11))) => }
      outcome.sideEffects should contain theSameElementsInOrderAs Seq(Reply(Done), Reply(Done), Reply(Done))
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
