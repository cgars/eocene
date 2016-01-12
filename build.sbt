name := """eocene"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += Resolver.sonatypeRepo("snapshots")
  
libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "mysql" % "mysql-connector-java" % "5.1.27",
  "ws.securesocial" % "securesocial_2.11" % "3.0-M3",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test",
  "org.specs2" %% "specs2-core" % "3.7" % "test",
  "org.mockito" % "mockito-all" % "1.9.5"
)

