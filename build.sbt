val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-notes",
    version := "0.2.0",
    scalaVersion := scala3Version,

    logLevel := Level.Warn,
    run / watchLogLevel := Level.Warn,
    test / watchLogLevel := Level.Warn,
    test / watchBeforeCommand := Watch.clearScreen,
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    Global / cancelable := true,
    semanticdbEnabled := true,

    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-new-syntax",
      "-print-lines",
      "-Yexplicit-nulls",
      "-Ysafe-init",
      ),

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,

    scalafixConfig := Some(file("project/scalafix.conf")),
    ThisBuild / scalafixDependencies ++= Seq(
      "io.github.ghostbuster91.scalafix-unified" %% "unified" % "0.0.8",
      "com.github.xuwei-k" %% "scalafix-rules" % "0.3.1",
      "net.pixiv" %% "scalafix-pixiv-rule" % "4.5.3",
    ),

    wartremoverErrors ++= Warts.unsafe.filterNot(Set(
      Wart.Var, Wart.Any
    ).contains),
    wartremoverErrors ++= Seq(
      Wart.ArrayEquals, Wart.AnyVal, Wart.ExplicitImplicitTypes,
      Wart.FinalCaseClass, Wart.ImplicitConversion, 
      Wart.JavaConversions, Wart.JavaSerializable, Wart.LeakingSealed, 
      Wart.Nothing, Wart.Option2Iterable, Wart.PublicInference,
    ),
    wartremoverErrors ++= Seq(
      ContribWart.DiscardedFuture, ContribWart.ExposedTuples, 
      ContribWart.NoNeedForMonad, ContribWart.OldTime,  
      ContribWart.SealedCaseClass, ContribWart.SomeApply, 
      ContribWart.UnintendedLaziness, 
    ),
    wartremover.WartRemover.dependsOnLocalProjectWarts(customWarts),
    wartremoverErrors ++= Seq(
      Wart.custom("customWarts.CharPlusAny"), Wart.custom("customWarts.CharMinusAny"),
      Wart.custom("customWarts.CharTimesAny"), Wart.custom("customWarts.CharDividedByAny"),
      Wart.custom("customWarts.CharEqualsAny"),
    ),
  )

lazy val customWarts = project.in(file(".warts")).settings(
  scalaVersion := scala3Version,
  libraryDependencies ++= Seq(
      "org.wartremover" % "wartremover" % wartremover.Wart.PluginVersion cross CrossVersion.full
  ),
)
