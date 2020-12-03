package io.opentelemetry.play.propagation.httptextformat

import scala.jdk.CollectionConverters._

import io.opentelemetry.context.propagation.TextMapPropagator

object MapGetter {
  val INSTANCE = new MapGetter
}

final class MapGetter private() extends TextMapPropagator.Getter[Map[String, String]] {
  override def get(map: Map[String, String], key: String): String = map.getOrElse(key, null)

  override def keys(carrier: Map[String, String]): java.lang.Iterable[String] = carrier.keys.asJava
}

