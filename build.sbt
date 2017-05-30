name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies += jdbc
libraryDependencies += cache
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
libraryDependencies += "net.debasishg" %% "redisclient" % "3.2"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "com.github.kstyrc" % "embedded-redis" % "0.6"
libraryDependencies += specs2 % Test

libraryDependencies += "com.webcohesion.ofx4j" % "ofx4j" % "1.8"

libraryDependencies ++= Seq(
  "com.typesafe.play.modules" %% "play-modules-redis" % "2.4.0",
  "com.github.etaty" %% "rediscala" % "1.6.0"
)