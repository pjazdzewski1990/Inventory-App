import sbt._
import Keys._

object InventoryBuild extends Build {

	val localMavenRepo = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
	val fwbrasilRepo = "fwbrasil.net" at "http://fwbrasil.net/maven/"

	val activateVersion = "1.7"
	val activateCore = "net.fwbrasil" %% "activate-core" % activateVersion
	val activatePrevayler = "net.fwbrasil" %% "activate-prevayler" % activateVersion
	val activateJdbc = "net.fwbrasil" %% "activate-jdbc" % activateVersion
	val activateMongo = "net.fwbrasil" %% "activate-mongo" % activateVersion
	val mysql = "mysql" % "mysql-connector-java" % "5.1.16"

	val akkaV       = "2.3.10"
	val akkaStreamV = "1.0-RC2"

	val actor = "com.typesafe.akka" %% "akka-actor" % akkaV
	val stream = "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV
	val http = "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV
	val scalaExperimental = "com.typesafe.akka" %% "akka-http-scala-experimental" % akkaStreamV
	val jsonExperimental = "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV

	val picklingDep = "org.scala-lang.modules" %% "scala-pickling" % "0.10.1"

	lazy val activateExample =
		Project(
			id = "inventory-app",
			base = file("."),
			settings = Defaults.defaultSettings ++ Seq(
				libraryDependencies ++= Seq(
					activateCore,
					activateJdbc,
					picklingDep,
					actor,
					stream,
					http,
					scalaExperimental),
				organization := "scalac.io",
				scalaVersion := "2.11.6",
				version := "0.1",
				resolvers ++= Seq(localMavenRepo, fwbrasilRepo)))

}