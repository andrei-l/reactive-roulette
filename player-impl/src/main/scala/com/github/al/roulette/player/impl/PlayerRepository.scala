package com.github.al.roulette.player.impl

import java.util.UUID

import akka.Done
import com.datastax.driver.core.PreparedStatement
import com.github.al.persistence.UUIDConversions
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}

import scala.concurrent.{ExecutionContext, Future}

class PlayerRepository(session: CassandraSession)(implicit ec: ExecutionContext) {
  def getPlayerIdByName(playerName: String): Future[UUID] = {
    session.selectOne(""" SELECT playerId FROM registeredPlayers WHERE playerName = ? ALLOW FILTERING """, playerName).map {
      case None => throw new IllegalStateException(s"No player found for playerName $playerName")
      case Some(row) => row.getUUID("playerId")
    }
  }
}

class PlayerEventReadSideProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[PlayerEvent]
    with UUIDConversions {

  private var insertRegisteredPlayerStatement: PreparedStatement = _

  override def aggregateTags: Set[AggregateEventTag[PlayerEvent]] = Set(PlayerEvent.Tag)

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[PlayerEvent] =
    readSide.builder[PlayerEvent]("playerEventOffset")
      .setGlobalPrepare(createTable)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[PlayerCreated] {
      case EventStreamElement(playerId, PlayerCreated(PlayerState(playerName, _)), _) => insertPlayer(playerId, playerName)
    }.build


  private def createTable() =
    for {
      _ <- session.executeCreateTable(
        """
        CREATE TABLE IF NOT EXISTS registeredPlayers (
          playerId UUID PRIMARY KEY,
          playerName text
        )
      """)
      _ <- session.executeCreateTable("""
          CREATE INDEX IF NOT EXISTS registeredPlayersIndex
            on registeredPlayers (playerName)
      """)

    } yield Done

  private def prepareStatements() =
    for {
      insertPlayerCreator <- session.prepare("INSERT INTO registeredPlayers(playerId, playerName) VALUES (?, ?)")
    } yield {
      insertRegisteredPlayerStatement = insertPlayerCreator
      Done
    }

  private def insertPlayer(playerId: UUID, playerName: String) =
    Future.successful(List(insertRegisteredPlayerStatement.bind(playerId, playerName)))

}
