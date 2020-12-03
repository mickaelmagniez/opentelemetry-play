package io.opentelemetry.play.actions

object RequestSpan {
  def apply(tracer: Tracer, request: RequestHeader): Span = {
    val extractedContext = OpenTelemetry.getGlobalPropagators.getTextMapPropagator.extract(Context.current(), request.headers.toSimpleMap, MapGetter.INSTANCE);

    val span = tracer.spanBuilder(Routes.endpointName(request).getOrElse(s"HTTP ${request.method}"))
      .setSpanKind(Span.Kind.SERVER)
      .setParent(extractedContext)
      .startSpan
    span.setAttribute("http.method", request.method)
    span.setAttribute("http.path", request.path)
    span.setAttribute("http.uri", request.uri)
    span.setAttribute("http.route", Routes.endpointName(request).getOrElse(s"HTTP ${request.method}"))
    span
  }
}