import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(

    "uk.gov.hmrc"             %% "simple-reactivemongo"     % "7.20.0-play-26",
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.1.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.1.0" % Test classifier "tests",
    "org.scalatest"           %% "scalatest"                % "3.0.8"                 % "cache",
    "com.typesafe.play"       %% "play-cache"                % current                 % "cache",
    "org.mockito"             % "mockito-all"               % "1.10.19"               % "cache",
    "com.github.tomakehurst"  % "wiremock"                  % "2.21.0"                % "cache",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "cache, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"                 % "cache, it"
  )
}
