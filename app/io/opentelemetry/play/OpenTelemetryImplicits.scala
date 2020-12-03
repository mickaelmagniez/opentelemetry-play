package io.opentelemetry.play

import io.opentelemetry.play.actions.{RequestSpan, TraceData, TracingRequest}
import play.api.mvc.Request

trait OpenTelemetryImplicits {

  val openTelemetry: OpenTelemetryService

  /**
   * Creates a trace data including a span from request headers.
   *
   * @param request the HTTP request header
   * @return the trace data
   */
  implicit def request2trace[A](implicit request: Request[A]): TraceData = {
    TraceData(
//      span = Some(
        request match {
          case r: TracingRequest[A] =>
            r.span
          case r =>
            RequestSpan(openTelemetry.tracer, r)
        }
//      )
    )
  }

}