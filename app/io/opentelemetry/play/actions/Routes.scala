package io.opentelemetry.play.actions

import play.api.mvc.RequestHeader
import play.api.routing.Router

object Routes {
  def endpointName(request: RequestHeader): Option[String] = request.attrs.get(Router.Attrs.HandlerDef).map { handler =>
    s"${handler.controller}.${handler.method}"
  }
}