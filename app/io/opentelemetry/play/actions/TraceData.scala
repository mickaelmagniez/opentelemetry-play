package io.opentelemetry.play.actions

import io.opentelemetry.api.trace.Span

trait TraceData {
  def span: Option[Span]
}

object TraceData extends LowPriorityTraceDataImplicits {
  def apply(span: Span): TraceData = {
    new DefaultTraceData(span)
  }
}

class DefaultTraceData(span: Span) extends TraceData {
  def span: Option[Span] = Option(span)
}


trait LowPriorityTraceDataImplicits {
  /**
   * A TraceData that returns None.  This is used as the "default" data if
   * no implicit TraceData is found in local scope (meaning there is nothing defined
   * through import or "implicit val").
   */
  implicit val NoTraceData: TraceData = TraceData(null)
  implicit def spanToTraceData(span: Span): TraceData = {
    TraceData(span)
  }
}