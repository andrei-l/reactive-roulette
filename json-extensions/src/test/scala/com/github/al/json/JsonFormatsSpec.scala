package com.github.al.json

import java.time.Duration
import java.util.UUID

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{Format, JsSuccess, Json}
import JsonFormats._

class JsonFormatsSpec extends WordSpec with Matchers {

  case class Sample(uuid: UUID, duration: Duration)

  implicit val sampleFormat: Format[Sample] = Json.format


  private val sample = Sample(UUID.fromString("7e595fac-830e-44f1-b73e-f8fd60594ace"), Duration.ofSeconds(30))
  private val sampleJsonObject = Json.obj("uuid" -> "7e595fac-830e-44f1-b73e-f8fd60594ace", "duration" -> "PT30S")

  "The JsonFormats" should {
    "write java.util.UUID and java.time.Duration to json properly" in {
      Json.toJson(sample) should ===(sampleJsonObject)
    }

    "read from json to java.util.UUID and java.time.Duration" in {
      Json.fromJson(sampleJsonObject)(sampleFormat) should ===(JsSuccess(sample))
    }
  }
}
