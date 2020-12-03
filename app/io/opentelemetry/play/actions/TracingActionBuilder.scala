
package io.opentelemetry.play.actions

import io.opentelemetry.api.trace.{StatusCode, Tracer}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * A class to make it easy to create Actions that take a TracingRequest
 *
 * Normally you would create an object that extends this class, and use that to create Actions.
 *
 * {{{
 * object TracingAction extends TracingActionBuilder(openTelemetry.tracer, parse.default)
 * }}}
 */
class TracingActionBuilder(protected[this] val tracer: Tracer, val parser: BodyParser[AnyContent])
                          (implicit ec: ExecutionContext) extends ActionBuilder[TracingRequest, AnyContent] {

  protected def finishSpan[A](request: TracingRequest[A], result: Result): Result = {
    request.span.setAttribute("http.status.code", result.header.status)
    request.span.setAttribute("http.status.text", result.header.reasonPhrase.getOrElse(""))
    result.header.status match {
      case s if s >= 200 && s <= 400 => request.span.setStatus(StatusCode.OK, s"HTTP Code: ${result.header.status}")
      case 401 => request.span.setStatus(StatusCode.ERROR, s"HTTP Code: ${result.header.status}")
      case 403 => request.span.setStatus(StatusCode.ERROR, s"HTTP Code: ${result.header.status}")
      case 404 => request.span.setStatus(StatusCode.ERROR, s"HTTP Code: ${result.header.status}")
      case s if s >= 500 => request.span.setStatus(StatusCode.ERROR, s"HTTP Code: ${result.header.status}")
    }
    request.span.`end`()
    result
  }

  final def invokeBlock[A](request: Request[A], block: TracingRequest[A] => Future[Result]): Future[Result] = {
    val span = RequestSpan(tracer, request)
    val tracingRequest = new TracingRequest(span, request)
    block(tracingRequest).map(result => {
      finishSpan(tracingRequest, result)
      result
    })
  }

  override protected def executionContext: ExecutionContext = ec

}

