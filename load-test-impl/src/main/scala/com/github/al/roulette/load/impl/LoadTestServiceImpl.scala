package com.github.al.roulette.load.impl

import java.time.Duration
import java.util.UUID

import akka.NotUsed
import akka.actor.Scheduler
import akka.http.javadsl.model.headers
import akka.stream.scaladsl.Source
import com.github.al.roulette.bet
import com.github.al.roulette.bet.api.{Bet, BetService}
import com.github.al.roulette.game.api.{Game, GameId, GameService}
import com.github.al.roulette.load.api.{LoadTestParameters, LoadTestService}
import com.github.al.roulette.player.api._
import com.github.al.roulette.winnings.api.WinningsService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}

import scala.concurrent.duration.DurationDouble
import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Random, Success, Try}

class LoadTestServiceImpl(gameService: GameService,
                          betService: BetService,
                          playerService: PlayerService,
                          winningsService: WinningsService,
                          pubSubRegistry: PubSubRegistry,
                          scheduler: Scheduler)(implicit executionContext: ExecutionContext)
  extends LoadTestService {
  private final val GameDefaultDuration = Duration.ofSeconds(10)
  private val loadTestEventsTopic = pubSubRegistry.refFor(TopicId[LoadTestEvent])

  override def startLoadTest: ServiceCall[LoadTestParameters, Source[String, NotUsed]] = {
    ServiceCall { parameters =>
      scheduler.scheduleOnce(1 second)(startLoadTest(parameters))
      Future.successful(loadTestEventsTopic.subscriber.map(_.msg))
    }
  }

  private def startLoadTest(parameters: LoadTestParameters): Unit = {
    loadTestEventsTopic.publish(LoadTestEvent(s"Load test started for next parameters: $parameters"))

    val gameIdsSequenceOfFutureTries = for {
      gameName <- 1 to parameters.numberOfConcurrentGames
      gameIdFuture = gameService.createGame.invoke(Game(s"$gameName", GameDefaultDuration))
    } yield futureToFutureTry(gameIdFuture)
    val gameIdsFutureTriesSequence = Future.sequence(gameIdsSequenceOfFutureTries)
    val gameIdsFutureSequence = getSuccessfulFutures(gameIdsFutureTriesSequence, "Successfully created a game")
    forAllFailureFutures(gameIdsFutureTriesSequence, "Failed to create a game")

    val playerIdsFuturesSequence = 1 to parameters.numberOfPlayers map (playerName => s"$playerName" -> playerService.registerPlayer.invoke(Player(s"$playerName")))

    val playerIdToAccessTokenSequenceOfFutureTries = for {
      (playerName, playerIdFuture) <- playerIdsFuturesSequence
      accessTokenFuture = playerService.login.invoke(PlayerCredentials(playerName))
      playerIdToAccessToken = for {playerId <- playerIdFuture; accessToken <- accessTokenFuture} yield playerId -> accessToken
    } yield futureToFutureTry(playerIdToAccessToken)

    val playerIdToAccessTokenFutureTriesSequence = Future.sequence(playerIdToAccessTokenSequenceOfFutureTries)
    val playerIdToAccessTokenFutureSequence = getSuccessfulFutures(playerIdToAccessTokenFutureTriesSequence, "Successfully created and logged in a user")
    forAllFailureFutures(playerIdToAccessTokenFutureTriesSequence, "Failed to create and login a user")

    val betsFuture = for {
      gameIdsSequence <- gameIdsFutureSequence
      playerIdsToAccessTokensSequence <- playerIdToAccessTokenFutureSequence
      bets <- placeBets(gameIdsSequence, playerIdsToAccessTokensSequence, parameters.numberOfConcurrentGames)
    } yield bets

    val betsSequence = getSuccessfulFutures(betsFuture, "Bet has been successfully put")
    forAllFailureFutures(betsFuture, "Failed to put a bet")
  }

  private def placeBets(gameIds: Seq[GameId], playerIdsToAccessTokens: Seq[(PlayerId, PlayerAccessToken)], numberOfBets: Int): Future[IndexedSeq[Try[NotUsed]]] = {
    val bets = for {
      _ <- 0 to numberOfBets
      gameId = gameIds(Random.nextInt(gameIds.length))
      (playerId, accessToken) = playerIdsToAccessTokens(Random.nextInt(playerIdsToAccessTokens.length))
      bet = placeBet(gameId.gameId, playerId.playerId, accessToken.token)
    } yield futureToFutureTry(bet)

    Future.sequence(bets)
  }

  private def placeBet(gameId: String, playerId: String, playerAccessToken: String): Future[NotUsed] = {
    def randomBet: Bet = {
      Random.nextInt(3) match {
        case 0 => Bet(Some(Random.nextInt(37)), bet.api.Number, Random.nextInt(100))
        case 1 => Bet(None, bet.api.Odd, Random.nextInt(4000))
        case 2 => Bet(None, bet.api.Even, Random.nextInt(2500))
      }
    }

    val jwtAuthorizationHeader = headers.Authorization.oauth2(playerAccessToken)
    betService
      .placeBet(gameId)
      .handleRequestHeader(header => header.addHeader(jwtAuthorizationHeader.name(), jwtAuthorizationHeader.value()))
      .invoke(randomBet)
  }

  private def getSuccessfulFutures[T](futureSeq: Future[IndexedSeq[Try[T]]], msg: String): Future[IndexedSeq[T]] =
    futureSeq.map(_.collect({ case Success(x) => loadTestEventsTopic.publish(LoadTestEvent(msg)); x }))

  private def forAllFailureFutures[T](futureSeq: Future[IndexedSeq[Try[T]]], msg: String): Unit =
    futureSeq.foreach(_.collect({ case Failure(e) => loadTestEventsTopic.publish(LoadTestEvent(s"$msg: ${e.getMessage}")) }))

  private def futureToFutureTry[T](f: Future[T]): Future[Try[T]] =
    f.map(Success(_)).recover({ case e => Failure(e) })

  private implicit def stringToUUID(s: String): UUID = UUID.fromString(s)

  private implicit def uuidToString(uuid: UUID): String = uuid.toString
}
