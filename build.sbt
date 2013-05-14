name := "srbench"

organization := "eu.planetdata"

version := "1.0.1"

scalaVersion := "2.10.1"

crossPaths := false

libraryDependencies ++= Seq(
  "es.upm.fi.oeg.morph.streams" % "esper-engine" % "1.0.1",
  "com.typesafe.slick" %% "slick" % "1.0.0",  
  "ch.qos.logback" % "logback-classic" % "1.0.9",
  "es.upm.fi.oeg.morph.streams" % "adapter-esper" % "1.0.5",
  "es.upm.fi.oeg.morph" % "query-rewriting" % "1.0.5",
  "es.upm.fi.oeg.morph" % "sparql-stream" % "1.0.3",
  "postgresql" % "postgresql" % "9.1-901.jdbc4"
)

resolvers ++= Seq(
  DefaultMavenRepository,
  "Local ivy Repository" at "file://"+Path.userHome.absolutePath+"/.ivy2/local",  
  "aldebaran-releases" at "http://aldebaran.dia.fi.upm.es/artifactory/sstreams-releases-local"
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

