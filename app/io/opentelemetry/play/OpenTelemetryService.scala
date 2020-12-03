package io.opentelemetry.play

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.{StatusCode, Tracer}
import io.opentelemetry.context.{Context, Scope}
import io.opentelemetry.play.actions.TraceData
import io.opentelemetry.play.propagation.httptextformat.MapSetter
import javax.inject.{Inject, Singleton}

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

@Singleton
class OpenTelemetryService @Inject()(val tracer: Tracer) {

  def getHeaders()(implicit traceData: TraceData): Map[String, String] = {
    val map: mutable.Map[String, String] = mutable.Map()
    traceData.span.foreach(span => {
      val scope = span.makeCurrent
      try {
        OpenTelemetry.getGlobalPropagators.getTextMapPropagator.inject(Context.current, map, MapSetter.INSTANCE)
      } finally {
        if (scope != null) scope.close()
      }
    }
    )
    map.toMap
  }

  def trace[A](traceName: String, tags: (String, String)*)(f: TraceData => A)(implicit parentData: TraceData): A = {
    val spanBuilder = tracer.spanBuilder(traceName)
    if (parentData.span.nonEmpty) spanBuilder.setParent(Context.current().`with`(parentData.span.get))
    val span = spanBuilder.startSpan
    Try(f(TraceData(span))) match {
      case Failure(t) =>
        span.recordException(t)
        span.setStatus(StatusCode.ERROR)
        span.`end`()
        throw t
      case Success(result) =>
        span.setStatus(StatusCode.OK)
        span.`end`()
        result
    }
  }

}
