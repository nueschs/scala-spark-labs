name := "Scala / Spark Labs"

version := "0.0.1"

scalaVersion := "2.11.12"

organization := "com.scigility"

scalacOptions ++= Seq(
  "-deprecation",           
  "-encoding", "UTF-8",
  "-feature",                
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",       
  "-Xlint",
  "-Yno-adapted-args",       
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",   
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import",
  "-Ypartial-unification"
)

val catsCoreVersion = "1.5.0"
val scalaCheckVersion = "1.14.0"
val scalaCheckShapelessVersion = "0.6.1"
val scalaTestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsCoreVersion,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test,
  "org.typelevel" %% "shapeless-scalacheck" % scalaCheckShapelessVersion % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)
