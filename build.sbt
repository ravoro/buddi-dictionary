name := """dict"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  ws,
  "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.jsoup" % "jsoup" % "1.10.1",
  "org.mockito" % "mockito-core" % "2.2.9",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1"
)

coverageExcludedPackages := "<empty>;router\\..*;views\\..*;repositories\\..*Table"
coverageMinimum := 80
coverageFailOnMinimum := false
coverageHighlighting := true
