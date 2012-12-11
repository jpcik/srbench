import sbt._
import Keys._

object HelloBuild extends Build {
  lazy val root = Project(id = "srbench",
                          base = file(".")) //aggregate(core, querygen,r2rmlTc)

//  lazy val core = Project(id = "morph-core",
//                          base = file("morph-core"))

}
