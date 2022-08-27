ThisBuild / scalaVersion := "3.1.3"
ThisBuild / version := "0.0.1-SNAPSHOT"
lazy val library =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("library"))
    .settings()
    .jvmSettings(
      libraryDependencies ++=
        Seq("org.scalatest" %% "scalatest" % "3.2.13" % Test)
    )

lazy val interpreter =
  project
    .in(file("interpreter"))
    .settings(
      libraryDependencies ++=
        Seq("org.jline" % "jline-reader" % "3.21.0", "org.scalatest" %% "scalatest" % "3.2.13" % Test)
    )
    .dependsOn(library.jvm)

lazy val webUI = project
  .in(file("webUI"))
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++=
      Seq(
        "org.scala-js" %%% "scalajs-dom" % "2.2.0",
        "com.github.japgolly.scalajs-react" %%% "core" % "2.1.1",
        "com.github.japgolly.scalajs-react" %%% "extra" % "2.1.1",
        "com.github.japgolly.scalacss" %%% "core" % "1.0.0",
        "com.github.japgolly.scalacss" %%% "ext-react" % "1.0.0",
        "org.scalatest" %% "scalatest" % "3.2.13" % Test
      )
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(library.js)

lazy val ideaPlugin =
  project
    .in(file("ideaPlugin"))
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      ThisBuild / intellijPluginName := "CraftingInterpreters",
      ThisBuild / intellijBuild := "213.6777.52",
      ThisBuild / intellijPlatform := IntelliJPlatform.IdeaCommunity,
      Global / intellijAttachSources := true,
      Compile / javacOptions ++= "--release" :: "11" :: Nil,
      intellijPlugins ++= Seq(
        "org.intellij.intelliLang".toPlugin,
        "com.intellij.platform.images".toPlugin
      ),
      libraryDependencies ++=
        Seq(
          ("com.eclipsesource.minimal-json" % "minimal-json" % "0.9.5").withSources(),
          "org.scala-lang.modules" % "scala-swing_3" % "3.0.0", // "2.1.1" ,
          "org.scala-lang" % "scala3-library_3" % "3.1.3",
          "org.typelevel" % "cats-effect_3" % "3.3.14",
          "org.scalatest" %% "scalatest" % "3.2.13" % Test
        )
    )
