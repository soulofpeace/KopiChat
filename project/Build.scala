import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "webchat"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    "postgresql" % "postgresql" % "9.1-901.jdbc4",
    "com.typesafe.slick" %% "slick" % "1.0.0"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )

}
