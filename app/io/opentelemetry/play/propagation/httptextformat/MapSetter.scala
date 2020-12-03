package io.opentelemetry.play.propagation.httptextformat

import io.opentelemetry.context.propagation.TextMapPropagator

import scala.collection.mutable

object MapSetter {
  val INSTANCE = new MapSetter
}

final class MapSetter private() extends TextMapPropagator.Setter[mutable.Map[String, String]] {
  override def set(map: mutable.Map[String, String], key: String, value: String): Unit = {
    map += (key -> value)
  }
}
