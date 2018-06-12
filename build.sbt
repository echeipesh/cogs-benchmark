// Rename this as you see fit
name := "cogs-benchmark"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.12"

organization := "com.azavea"

licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:existentials",
  "-language:experimental.macros",
  "-feature",
  "-Ypartial-unification" // Required by Cats
)

addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.4" cross CrossVersion.binary)
addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }

resolvers ++= Seq(
  "locationtech-releases" at "https://repo.locationtech.org/content/groups/releases"
  // "locationtech-snapshots" at "https://repo.locationtech.org/content/groups/snapshots"
)

val gtVersion = "2.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.locationtech.geotrellis" %% "geotrellis-spark" % gtVersion,
  "org.locationtech.geotrellis" %% "geotrellis-s3"    % gtVersion,
  "org.apache.spark" %% "spark-core" % "2.3.0" % Provided,
  "org.scalatest"    %%  "scalatest" % "2.2.3" % Test
)

assemblyShadeRules in assembly := {
  val shadePackage = "com.azavea.shaded.demo"
  Seq(
    ShadeRule.rename("org.apache.avro.**" -> s"$shadePackage.org.apache.avro.@1").inLibrary("com.azavea.geotrellis" %% "geotrellis-spark" % gtVersion).inAll,
    ShadeRule.rename("shapeless.**" -> s"$shadePackage.shapeless.@1").inAll
  )
}

assemblyMergeStrategy in assembly := {
  case s if s.startsWith("META-INF/services") => MergeStrategy.concat
  case "reference.conf" | "application.conf"  => MergeStrategy.concat
  case "META-INF/MANIFEST.MF" | "META-INF\\MANIFEST.MF" => MergeStrategy.discard
  case "META-INF/ECLIPSEF.RSA" | "META-INF/ECLIPSEF.SF" => MergeStrategy.discard
  case _ => MergeStrategy.first
}
