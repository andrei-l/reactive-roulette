package com.github.al.roulette.bet.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.github.al.persistence.UUIDConversions
import com.github.al.roulette.bet
import com.github.al.roulette.bet.BetSerializerRegistry
import com.github.al.roulette.bet.api.Bet
import com.github.al.roulette.test.persistence.EntitySpecSugar
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver.Reply
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

class RouletteGameBetsEntitySpec
  extends WordSpec
    with Matchers with BeforeAndAfterAll with OptionValues with EntitySpecSugar with UUIDConversions {
  override type P = RouletteGameBetsEntity

  override final val persistenceEntity = new RouletteGameBetsEntity
  override final val persistenceEntityId = "0093563a-3d57-4cd3-89c9-2576cbd824bc"
  final val PlayerId = "f7bcf04b-d9a2-4b0b-b6e5-4ecdeb1a969b"
  private final val SampleBet = Bet(Some(2), bet.api.Number, 34.32)
  private final val SamplePlayerBet = PlayerBet(PlayerId, SampleBet)

  private implicit val system = ActorSystem("test", JsonSerializerRegistry.actorSystemSetupFor(BetSerializerRegistry))

  "The roulette game bets entity " should {
    "allow starting betting" in withDriver { driver =>
      val outcome = driver.run(StartGameBetting)
      outcome.events should contain only GameBettingStarted
      outcome.state should ===(Some(RouletteGameBetsState()))
      outcome.sideEffects should contain only Reply(Done)
    }

    "allow starting betting and placing a bet" in withDriver { driver =>
      val outcome = driver.run(StartGameBetting, PlaceBet(PlayerId, SampleBet))
      outcome.events should contain only(GameBettingStarted, BetPlaced(SamplePlayerBet))
      outcome.state should ===(Some(RouletteGameBetsState(List(SamplePlayerBet))))
      outcome.sideEffects should contain theSameElementsInOrderAs Seq(Reply(Done), Reply(Done))
    }

    "allow starting betting and finishing betting" in withDriver { driver =>
      val outcome = driver.run(StartGameBetting, FinishGameBetting)
      outcome.events should contain only(GameBettingStarted, GameBettingFinished(Nil))
      outcome.state should ===(Some(RouletteGameBetsState(Nil, gameFinished = true)))
      outcome.sideEffects should contain theSameElementsInOrderAs Seq(Reply(Done), Reply(Done))
    }

    "allow starting betting placing a bet and finishing betting" in withDriver { driver =>
      val outcome = driver.run(StartGameBetting, PlaceBet(PlayerId, SampleBet), FinishGameBetting)
      outcome.events should contain only(GameBettingStarted, BetPlaced(SamplePlayerBet), GameBettingFinished(List(SamplePlayerBet)))
      outcome.state should ===(Some(RouletteGameBetsState(List(SamplePlayerBet), gameFinished = true)))
      outcome.sideEffects should contain theSameElementsInOrderAs Seq(Reply(Done), Reply(Done), Reply(Done))
    }
  }

  protected override def afterAll: Unit = TestKit.shutdownActorSystem(system)
}

