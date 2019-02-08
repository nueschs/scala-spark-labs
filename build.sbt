import sbtassembly.AssemblyPlugin.autoImport.ShadeRule
import sbtassembly.MergeStrategy

name := "Scala / Spark Labs"

version := "0.0.1"

scalaVersion := "2.11.12"
ensimeScalaVersion in ThisBuild := "2.11.12"
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")
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
val scalaCheckShapelessVersion = "1.2.0-1"
val scalaTestVersion = "3.0.5"
val sparkVersion = "2.4.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsCoreVersion,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test,
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapelessVersion % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.apache.spark"    %% "spark-streaming"            % sparkVersion % "provided", //LICENCE: Apache License 2.0
  "org.apache.spark"    %% "spark-sql"                  % sparkVersion % "provided", //LICENCE: Apache License 2.0
  "org.apache.spark"    %% "spark-streaming-kafka-0-10" % sparkVersion,
  "org.typelevel" %% "cats-effect" % "1.2.0",
  "com.github.pureconfig" %% "pureconfig" % "0.10.1",
  "org.scalikejdbc" %% "scalikejdbc"       % "3.3.2"
)

val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)


assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("org.apache.avro.**" -> "org.apache.avro_shaded.@1").inAll,
  ShadeRule.rename("shapeless.**" -> "shapeless_shaded.@1").inAll//,
 // ShadeRule.rename("cats.effect.**" -> "cats.effect_shaded.@1").inAll //can be removed once http4s can be bumped to version 19 which relies on FS2 1.0.0 and cats-effect 1.0.0
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList(xs @ _*) if xs.last.endsWith(".html") => MergeStrategy.discard
  case PathList(xs @ _*) if xs.last.endsWith("library.properties") => MergeStrategy.last
  case "application.conf"            => MergeStrategy.concat
  case "reference.conf"              => MergeStrategy.concat
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
  case PathList("org", "aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("scalaz", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
  case PathList("scala", xs @ _*) => MergeStrategy.last
  case x =>
    val baseStrategy = (assemblyMergeStrategy in assembly).value
    baseStrategy(x)
}
