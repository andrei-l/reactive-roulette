package com.github.al.json

import java.time.Duration
import java.util.UUID

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsString, Reads, Writes}

import scala.util.Try

object JsonFormats {

  implicit val uuidReads: Reads[UUID] = implicitly[Reads[String]]
    .collect(ValidationError("Invalid UUID"))(Function.unlift { str =>
      Try(UUID.fromString(str)).toOption
    })

  implicit val uuidWrites: Writes[UUID] = Writes { uuid =>
    JsString(uuid.toString)
  }

  implicit val durationReads: Reads[Duration] = implicitly[Reads[String]]
    .collect(ValidationError("Invalid duration"))(Function.unlift { str =>
      Try(Duration.parse(str)).toOption
    })

  implicit val durationWrites: Writes[Duration] = Writes { duration =>
    JsString(duration.toString)
  }

}
