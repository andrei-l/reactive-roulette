package com.github.al.roulette.load.impl

import java.time.Duration
import java.util.UUID
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Scheduler
import akka.http.javadsl.model.headers
import akka.stream.scaladsl.{Flow, Source}
import akka.{Done, NotUsed}
import com.github.al.roulette.bet.api.{Bet, BetService}
import com.github.al.roulette.game.api.{Game, GameEvent, GameId, GameService}
import com.github.al.roulette.load.api.{LoadTestParameters, LoadTestService}
import com.github.al.roulette.load.impl.FutureExtension._
import com.github.al.roulette.player.api._
import com.github.al.roulette.winnings.api.WinningsService
import com.github.al.roulette.{bet, game}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}

import scala.concurrent.duration.DurationDouble
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Random, Try}

class LoadTestServiceImpl(gameService: GameService, betService: BetService,
                          playerService: PlayerService, winningsService: WinningsService,
                          pubSubRegistry: PubSubRegistry, scheduler: Scheduler)
                         (implicit executionContext: ExecutionContext)
  extends LoadTestService {
  private final val GameDefaultDuration = Duration.ofSeconds(10)
  private lazy val loadTestEventsTopic = pubSubRegistry.refFor(TopicId[LoadTestEvent])
  private lazy val throttlingAccumulator = ThrottlingAccumulator(scheduler, publishEvent)

  private final val PlayersCounter = new AtomicInteger(0)
  private final val GamesCounter = new AtomicInteger(0)

  private final val GamesToPlay = new ArrayBlockingQueue[UUID](50)
  private final val FinishedGames = ConcurrentHashMap.newKeySet[UUID]()

  gameService.gameEvents.subscribe.atLeastOnce(Flow[GameEvent].map {
    case e: game.api.GameStarted => GamesToPlay.add(e.gameId); Done
    case e: game.api.GameFinished => FinishedGames.add(e.gameId); Done
    case _ => Done
  })


  override def startLoadTest: ServiceCall[LoadTestParameters, Source[String, NotUsed]] = {
    GamesToPlay.clear()
    FinishedGames.clear()

    ServiceCall { parameters =>
      scheduler.scheduleOnce(1 second)(startLoadTest(parameters))
      Future.successful(loadTestEventsTopic.subscriber.map(_.msg))
    }
  }

  private def startLoadTest(parameters: LoadTestParameters): Unit = {
    loadTestEventsTopic.publish(LoadTestEvent(s"Load test started with next parameters: $parameters"))

    val playerIds: IndexedSeq[(String, Future[PlayerId])] = createPlayers(parameters.numberOfPlayers)
    val playerIdsWithAccessToken: Future[IndexedSeq[(PlayerId, PlayerAccessToken)]] = loginPlayers(playerIds)
    val gameIds: Future[IndexedSeq[GameId]] = createGames(parameters.numberOfConcurrentGames)

    startPlacingBets(playerIdsWithAccessToken, parameters.numberOfBetsToPlace)
  }

  private def loginPlayers(playerIdsFuturesSequence: IndexedSeq[(String, Future[PlayerId])]) = {
    val playerIdToAccessTokenSequenceOfFutureTries = for {
      (playerName, playerIdFuture) <- playerIdsFuturesSequence
      accessTokenFuture = playerService.login.invoke(PlayerCredentials(playerName))
      playerIdToAccessToken = for {playerId <- playerIdFuture; accessToken <- accessTokenFuture} yield playerId -> accessToken
    } yield playerIdToAccessToken.toFutureTry

    val playerIdToAccessTokenFutureTriesSequence = Future.sequence(playerIdToAccessTokenSequenceOfFutureTries)
    val playerIdToAccessTokenFutureSequence = playerIdToAccessTokenFutureTriesSequence.getSuccessfulFutures(enqueueMsg("Successfully created and logged in a user"))
    playerIdToAccessTokenFutureTriesSequence.forAllFailureFutures(msg => enqueueMsg(s"Failed to create and login a user:$msg"))
    playerIdToAccessTokenFutureSequence
  }

  private def createPlayers(numberOfPlayers: Int): IndexedSeq[(String, Future[PlayerId])] = {
    val playerIdsFuturesSequence = PlayersCounter.get() until numberOfPlayers map {
      playerName => s"$playerName" -> playerService.registerPlayer.invoke(Player(s"$playerName"))
    }
    playerIdsFuturesSequence
  }

  private def createGames(numberOfConcurrentGames: Int) = {
    val gameIdsSequenceOfFutureTries = for {
      gameName <- GamesCounter.get() until GamesCounter.addAndGet(numberOfConcurrentGames)
      gameIdFuture = gameService.createGame.invoke(Game(s"$gameName", GameDefaultDuration))
    } yield gameIdFuture.toFutureTry

    val gameIdsFutureTriesSequence = Future.sequence(gameIdsSequenceOfFutureTries)
    val gameIdsFutureSequence = gameIdsFutureTriesSequence.getSuccessfulFutures(enqueueMsg("Successfully created a game"))
    gameIdsFutureTriesSequence.forAllFailureFutures(msg => enqueueMsg(s"Failed to create a game:$msg"))
    gameIdsFutureSequence
  }

  private def startPlacingBets(playerIdToAccessTokenFutureSequence: Future[IndexedSeq[(PlayerId, PlayerAccessToken)]],
                               numberOfBetsToPlace: Int) = {
    val betsFuture = for {
      playerIdsToAccessTokensSequence <- playerIdToAccessTokenFutureSequence
      bets <- placeBets(playerIdsToAccessTokensSequence, numberOfBetsToPlace)
    } yield bets

    betsFuture.forAllFailureFutures(msg => enqueueMsg(s"Failed to put a bet:$msg"))
    Await.ready(betsFuture.getSuccessfulFutures(publishEvent("Bet has been successfully put")), 30 seconds)
  }

  private def placeBets(playerIdsToAccessTokens: Seq[(PlayerId, PlayerAccessToken)], numberOfBets: Int): Future[IndexedSeq[Try[NotUsed]]] = {
    def pollForNextNotPlayedGame: Option[UUID] =
      Try(GamesToPlay.poll(15, TimeUnit.SECONDS)).toOption match {
        case option@Some(gameId) => if (!FinishedGames.contains(gameId)) option else pollForNextNotPlayedGame
        case none => none
      }

    val bets = for {
      _ <- 0 to numberOfBets
      gameIdOption = pollForNextNotPlayedGame
      (playerId, accessToken) = playerIdsToAccessTokens(Random.nextInt(playerIdsToAccessTokens.length))
      bet = gameIdOption match {
        case Some(gameId) =>
          val placeBetResult = placeBet(gameId, playerId.playerId, accessToken.token)
          if (!FinishedGames.contains(gameId)) GamesToPlay.add(gameId)
          placeBetResult
        case None => Future.successful(NotUsed)
      }
    } yield bet.toFutureTry

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

  private def enqueueMsg(msg: String) = throttlingAccumulator.enqueue(msg)

  private def publishEvent(s: String): Unit = loadTestEventsTopic.publish(LoadTestEvent(s))

  private implicit def stringToUUID(s: String): UUID = UUID.fromString(s)

  private implicit def uuidToString(uuid: UUID): String = uuid.toString
}
