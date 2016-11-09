name := """dict"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies += "org.jsoup" % "jsoup" % "1.10.1"
libraryDependencies += "org.mockito" % "mockito-core" % "2.2.9"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1"
//libraryDependencies += jdbc
//libraryDependencies += cache
//libraryDependencies += ws
