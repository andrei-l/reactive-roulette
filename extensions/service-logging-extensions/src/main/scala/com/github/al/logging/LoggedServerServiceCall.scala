package com.github.al.logging

import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.typesafe.scalalogging.LazyLogging

object LoggedServerServiceCall extends LazyLogging {
  def logged[Request, Response](serviceCall: ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] =
    ServerServiceCall.compose { requestHeader =>
      logger.info(s"Received ${requestHeader.method} ${requestHeader.uri}")
      serviceCall
    }
}
