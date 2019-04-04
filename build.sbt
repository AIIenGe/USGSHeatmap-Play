name := """play-earthquake-v1"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.8"

javacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-parameters",
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "-Werror"
)

crossScalaVersions := Seq("2.11.12", "2.12.7")

libraryDependencies += guice

libraryDependencies += "com.google.code.gson" % "gson" % "2.8.5"

// Test Database
libraryDependencies += "com.h2database" % "h2" % "1.4.197"

// Testing libraries for dealing with CompletionStage...
libraryDependencies += "org.assertj" % "assertj-core" % "3.11.1" % Test
libraryDependencies += "org.awaitility" % "awaitility" % "3.1.3" % Test

// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))

herokuAppName in Compile := "glacial-mesa-23065"

lazy val root = (project in file(".")).enablePlugins(PlayJava, SbtWeb)
