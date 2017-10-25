name := """dict"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.11.7"


libraryDependencies ++= Seq(
  "org.jsoup" % "jsoup" % "1.10.1",
  "org.mockito" % "mockito-core" % "2.2.9",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1",
  "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3"
)
