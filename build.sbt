name := "srbench"

organization := "eu.planetdata"

version := "1.0.0"

scalaVersion := "2.10.1"

crossPaths := false

libraryDependencies ++= Seq(
  //"org.scalaj" %% "scalaj-time" % "0.6",
  "joda-time" % "joda-time" % "2.2",
  "org.joda" % "joda-convert" % "1.3.1",
  "es.upm.fi.oeg.morph.streams" % "esper-engine" % "1.0.1", 
  "ch.qos.logback" % "logback-classic" % "1.0.9",
  "es.upm.fi.oeg.morph" % "sparql-stream" % "1.0.2",
  "es.upm.fi.oeg.morph.streams" % "adapter-esper" % "1.0.4" exclude("org.slf4j","slf4j-log4j12") exclude("org.slf4j","slf4j-api"),
  "postgresql" % "postgresql" % "9.1-901.jdbc4"
)

resolvers ++= Seq(
  DefaultMavenRepository,
  "Local ivy Repository" at "file://"+Path.userHome.absolutePath+"/.ivy2/local",  
  "aldebaran-releases" at "http://aldebaran.dia.fi.upm.es/artifactory/sstreams-releases-local"
 )

scalacOptions += "-deprecation"

EclipseKeys.skipParents in ThisBuild := false

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_))

unmanagedSourceDirectories in Test <<= (scalaSource in Test)(Seq(_))

