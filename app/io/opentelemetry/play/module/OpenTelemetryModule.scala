package io.opentelemetry.play.module

import io.grpc.ManagedChannelBuilder
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.HttpTraceContext
import io.opentelemetry.context.propagation.DefaultContextPropagators
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.TracerSdkProvider

import java.util
//import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.sdk.trace.`export`.{BatchSpanProcessor, SpanExporter}
import io.opentelemetry.sdk.trace.data.SpanData
import play.api.Configuration
import play.api.inject.{ApplicationLifecycle, SimpleModule, bind}

import javax.inject.{Inject, Provider}
import scala.concurrent.Future

/**
 * An Open Telemetry module.
 *
 * This module can be registered with Play automatically by appending it in application.conf:
 * {{{
 *   play.modules.enabled += "io.opentelemetry.play.module.OpenTelemetryModule"
 * }}}
 *
 */
class OpenTelemetryModule extends SimpleModule((env, conf) =>
  Seq(
    if (conf.getOptional[Boolean]("opentelemetry.enabled").getOrElse(false)) {
      bind[Tracer].toProvider(classOf[TracingProvider])
    } else {
      bind[Tracer].toProvider(classOf[NoopTracingProvider])
    }
  ) :+ (conf.getOptional[String]("opentelemetry.jaeger.host") match {
    case None => bind[SpanExporter].toProvider(classOf[NoopSpanExporterProvider])
    case Some(_) => bind[SpanExporter].toProvider(classOf[JaegerSpanExporterProvider])
  })
)

class NoopSpanExporter extends SpanExporter {
  override def `export`(spans: util.Collection[SpanData]): CompletableResultCode = new CompletableResultCode().succeed

  override def flush(): CompletableResultCode = new CompletableResultCode().succeed

  override def shutdown(): CompletableResultCode = new CompletableResultCode().succeed
}

class NoopSpanExporterProvider @Inject()() extends Provider[NoopSpanExporter] {
  override def get(): NoopSpanExporter = new NoopSpanExporter()
}

class JaegerSpanExporterProvider @Inject()(conf: Configuration, lifecycle: ApplicationLifecycle) extends Provider[SpanExporter] {
  override def get(): SpanExporter = {
    val exporter = JaegerGrpcSpanExporter.builder()
      .setServiceName(conf.get[String]("opentelemetry.jaeger.service-name"))
      .setChannel(ManagedChannelBuilder.forAddress(conf.get[String]("opentelemetry.jaeger.host"), conf.get[Int]("opentelemetry.jaeger.port")).usePlaintext().build())
      .build
    lifecycle.addStopHook(() => Future.successful(try {
      exporter.shutdown()
    } catch {
      case _: Exception =>
    }))
    exporter
  }
}

class NoopTracingProvider @Inject()() extends Provider[Tracer] {
  override def get(): Tracer = Tracer.getDefault
}

class TracingProvider @Inject()(sender: Provider[SpanExporter],
                                lifecycle: ApplicationLifecycle,
                                configuration: Configuration)
  extends Provider[Tracer] {

  override def get(): Tracer = {

    OpenTelemetry.setGlobalPropagators(
      DefaultContextPropagators.builder().addTextMapPropagator(HttpTraceContext.getInstance()).build()
    )

    val tracerSdk = TracerSdkProvider.builder().build()
    if (configuration.get[Boolean]("opentelemetry.enabled")) {
      tracerSdk.addSpanProcessor(BatchSpanProcessor.builder(sender.get()).build)
    }
    val tracer = tracerSdk.get("io.opentelemetry.play")
    lifecycle.addStopHook(() => Future.successful(try {
      tracerSdk.forceFlush()
      tracerSdk.shutdown()
    } catch {
      case _: Exception =>
    }
    ))
    tracer

  }
}
