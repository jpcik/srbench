name := "srbench"

organization := "eu.planetdata"

version := "1.0.1"

scalaVersion := "2.10.1"

crossPaths := false

libraryDependencies ++= Seq(
  "es.upm.fi.oeg.morph.streams" % "esper-engine" % "1.0.1",
  "es.upm.fi.oeg.morph.streams" % "stream-reasoning" % "1.0.1",
  "com.typesafe.slick" %% "slick" % "1.0.0",  
  "ch.qos.logback" % "logback-classic" % "1.0.9",
  "es.upm.fi.oeg.morph" % "kyrie" % "0.18.1",
  "es.upm.fi.oeg.morph" % "sparql-stream" % "1.0.3",
  "es.upm.fi.oeg.morph.streams" % "adapter-esper" % "1.0.6",
  "es.upm.fi.oeg.morph" % "query-rewriting" % "1.0.6",
  "eu.trowl" % "trowl-core" % "1.2",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
  "org.scalacheck" % "scalacheck_2.10" % "1.10.0" % "test"
)

resolvers ++= Seq(
  DefaultMavenRepository,
  "Local ivy Repository" at "file://"+Path.userHome.absolutePath+"/.ivy2/local",  
  "aldebaran-releases" at "http://aldebaran.dia.fi.upm.es/artifactory/sstreams-releases-local",
  "aldebaran-libs" at "http://aldebaran.dia.fi.upm.es/artifactory/sstreams-external-libs-local",
  "nightlabs" at "http://dev.nightlabs.org/maven-repository/repo",
  "jmora" at "https://dl.dropboxusercontent.com/u/452942/maven"  
 )

publishTo := Some("Artifactory Realm" at "http://aldebaran.dia.fi.upm.es/artifactory/sstreams-releases-local")
    
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
    
publishMavenStyle := true
    
publishArtifact in (Compile, packageSrc) := false 

scalacOptions += "-deprecation"

EclipseKeys.skipParents in ThisBuild := false

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_))

unmanagedSourceDirectories in Test <<= (scalaSource in Test)(Seq(_))

