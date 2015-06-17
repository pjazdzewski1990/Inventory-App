import sbt._
import Keys._

object InventoryBuild extends Build {

	val localMavenRepo = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
	val fwbrasilRepo = "fwbrasil.net" at "http://fwbrasil.net/maven/"

	val activateVersion = "1.7"

	val akkaV       = "2.3.10"
	val akkaStreamV = "1.0-RC2"

	lazy val activateExample =
		Project(
			id = "inventory-app",
			base = file("."),
			settings = Defaults.defaultSettings ++ Seq(
				libraryDependencies ++= Seq(
					"net.fwbrasil" %% "activate-core" % activateVersion,
					"net.fwbrasil" %% "activate-jdbc" % activateVersion,
					"org.scala-lang.modules" %% "scala-pickling" % "0.10.1",
					"com.typesafe.akka" %% "akka-actor" % akkaV,
					"com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
					"com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV,
					"com.typesafe.akka" %% "akka-http-scala-experimental" % akkaStreamV,
					"com.typesafe.slick" %% "slick" % "2.1.0",
					"com.h2database" % "h2" % "1.4.186"),
				organization := "scalac.io",
				scalaVersion := "2.11.6",
				version := "0.1",
				resolvers ++= Seq(localMavenRepo, fwbrasilRepo)))

}