package com.github.al.roulette.winnings.impl

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.github.al.persistence.UUIDConversions
import com.github.al.roulette.bet
import com.github.al.roulette.bet.api.{Bet, PlayerBets}
import com.github.al.roulette.test.persistence.EntitySpecSugar
import com.github.al.roulette.winnings.WinningsSerializerRegistry
import com.github.al.roulette.winnings.api.PlayerWinning
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver.Reply
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

class WinningsEntitySpec
  extends WordSpec
    with Matchers with BeforeAndAfterAll with OptionValues with EntitySpecSugar with UUIDConversions {
  override type P = WinningsEntity

  override final val persistenceEntity = new WinningsEntity
  override final val persistenceEntityId = " 0dec1316-8aeb-4e31-a6b8-33e7166b999a"

  private final val PlayerId: UUID = "7f43664a-9177-47e7-95f5-e9ea3c2d916a"
  private final val PlayerId2: UUID = "a42fa8f3-d84d-4421-9d6d-cc04abbd2402"

  private final val Bets = PlayerBets(PlayerId, List(Bet(Some(4), bet.api.Number, 13), Bet(None, bet.api.Even, 2)))
  private final val Bets2 = PlayerBets(PlayerId2, List(Bet(Some(2), bet.api.Number, 7), Bet(None, bet.api.Odd, 11)))

  private final val winning: Int => PlayerWinning = {
    case 4 => PlayerWinning(PlayerId, 13 * 36 + 2 * 2)
    case 2 => PlayerWinning(PlayerId, 2 * 2)
    case 3 => PlayerWinning(PlayerId, 0)
  }

  private final val winning2: Int => PlayerWinning = {
    case 4 => PlayerWinning(PlayerId2, 0)
    case 2 => PlayerWinning(PlayerId2, 7 * 36)
    case 3 => PlayerWinning(PlayerId2, 11 * 2)
  }

  private final val winnings: Int => List[PlayerWinning] =
    number => List(winning(number), winning2(number))

  private implicit val system = ActorSystem("test", JsonSerializerRegistry.actorSystemSetupFor(WinningsSerializerRegistry))

  "The winnings entity" should {
    "allow saving game result" in withDriver { driver =>
      val outcome = driver.run(SaveGameResult(11))
      outcome.events should contain only GameResultSaved(11)
      outcome.state should ===(WinningsState(Some(11), None, None))
      outcome.sideEffects should contain only Reply(Done)
    }

    "allow saving players bets" in withDriver { driver =>
      val outcome = driver.run(SavePlayersBets(List(Bets, Bets2)))
      outcome.events should contain only PlayersBetsSaved(List(Bets, Bets2))
      outcome.state should ===(WinningsState(None, Some(List(Bets, Bets2)), None))
      outcome.sideEffects should contain only Reply(Done)
    }

    "emit WinningsCalculated event when saving player bets after saved winning number" in withDriver { driver =>
      val outcome = driver.run(SaveGameResult(4), SavePlayersBets(List(Bets, Bets2)))
      outcome.events should contain allElementsOf Seq(GameResultSaved(4), PlayersBetsSaved(List(Bets, Bets2)), WinningsCalculated(winnings(4)))
      outcome.state should ===(WinningsState(Some(4), Some(List(Bets, Bets2)), Some(winnings(4))))
      outcome.sideEffects should contain only Reply(Done)
    }

    "emit WinningsCalculated event when saving winning number after saved player bets" in withDriver { driver =>
      val outcome = driver.run(SavePlayersBets(List(Bets, Bets2)), SaveGameResult(2))
      outcome.events should contain allElementsOf Seq(PlayersBetsSaved(List(Bets, Bets2)), GameResultSaved(2), WinningsCalculated(winnings(2)))
      outcome.state should ===(WinningsState(Some(2), Some(List(Bets, Bets2)), Some(winnings(2))))
      outcome.sideEffects should contain only Reply(Done)
    }

    "properly calculate winnings for odd number" in withDriver { driver =>
      val outcome = driver.run(SavePlayersBets(List(Bets, Bets2)), SaveGameResult(3))
      outcome.events should contain allElementsOf Seq(PlayersBetsSaved(List(Bets, Bets2)), GameResultSaved(3), WinningsCalculated(winnings(3)))
      outcome.state should ===(WinningsState(Some(3), Some(List(Bets, Bets2)), Some(winnings(3))))
      outcome.sideEffects should contain only Reply(Done)
    }
  }

  protected override def afterAll: Unit = TestKit.shutdownActorSystem(system)
}
