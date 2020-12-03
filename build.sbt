name := "opentelemetry-play"
organization := "io.opentelemetry"

scalaVersion := "2.13.3"
crossScalaVersions := Seq("2.13.3")

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .settings(scalacOptions += "-Wunused:imports")
  .settings(scalacOptions += s"-Wconf:src=${target.value}/.*:s")

version := sys.env.getOrElse("BUILD_NUMBER", "0.0.0")

libraryDependencies ++= Seq(ws, specs2 % Test, guice)

libraryDependencies += "io.opentelemetry" % "opentelemetry-api" % "0.10.0"
libraryDependencies += "io.opentelemetry" % "opentelemetry-exporter-jaeger" % "0.10.0"
libraryDependencies += "io.grpc" % "grpc-netty-shaded" % "1.34.0"

