name := "buddi"
version := "1.0"
scalaVersion := "2.11.11"


libraryDependencies ++= Seq(
  "org.jsoup" % "jsoup" % "1.10.3",
  "org.mockito" % "mockito-core" % "2.11.0",
  "mysql" % "mysql-connector-java" % "5.1.44",

  // Play-specific
  ws,
  "com.adrianhurt" %% "play-bootstrap" % "1.1.1-P25-B3",
  "com.typesafe.play" %% "play-slick" % "2.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1"
)


coverageExcludedPackages := "<empty>;router\\..*;views\\..*;repositories\\..*Table"
coverageMinimum := 80
coverageFailOnMinimum := false
coverageHighlighting := true


lazy val root = (project in file(".")).enablePlugins(PlayScala)
