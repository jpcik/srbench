name := "srbench"

organization := "eu.planetdata"

version := "1.0.0"

scalaVersion := "2.9.1"

crossPaths := false

libraryDependencies ++= Seq(
  "com.hp.hpl.jena" % "jena" % "2.6.4",
  "es.upm.fi.oeg.morph" % "morph-core" % "1.0.1",
  "es.upm.fi.oeg.morph" % "morph-querygen" % "1.0.1",
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

