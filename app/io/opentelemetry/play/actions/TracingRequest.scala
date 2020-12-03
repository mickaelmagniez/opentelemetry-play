package io.opentelemetry.play.actions

import io.opentelemetry.api.trace.Span
import play.api.mvc.{Request, WrappedRequest}

class TracingRequest[+A](val span: Span, request: Request[A]) extends WrappedRequest(request)
