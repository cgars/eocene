name := """eocene"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers += Resolver.sonatypeRepo("snapshots")
  
libraryDependencies ++= Seq(
  jdbc,
  cache,
  "ws.securesocial" % "securesocial_2.11" % "3.0-M4",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test",
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "mysql" % "mysql-connector-java" % "5.1.27",
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  "com.typesafe.play" %% "play-mailer" % "3.0.1"
)

