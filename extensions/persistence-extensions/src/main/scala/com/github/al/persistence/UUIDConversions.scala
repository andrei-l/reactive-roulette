package com.github.al.persistence

import java.util.UUID

import scala.language.implicitConversions

trait UUIDConversions {
  protected implicit def stringToUUID(s: String): UUID = UUID.fromString(s)

  protected implicit def uuidToString(uuid: UUID): String = uuid.toString
}
