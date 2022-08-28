ThisBuild / scalaVersion := "3.1.3"
ThisBuild / version := "0.0.1-SNAPSHOT"
lazy val library =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("library"))
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
    fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly("LoxWebUI"),
    fastOptJS / webpackConfigFile := Some(baseDirectory.value / "dev.webpack.config.js"),
    fastOptJS / webpackDevServerExtraArgs ++= Seq(
      "--static-directory",
      (Compile / fastOptJS / artifactPath).value.getParentFile.getAbsolutePath
    ),
    webpackDevServerPort := 3000,
    webpackResources := webpackResources.value +++ ((Compile / resourceDirectory).value * ("*.scss"||"*.html")),
    webpack / version := "5.74.0",
    startWebpackDevServer / version := "4.10.0",
    webpackMonitoredFiles / includeFilter := "**/*.{js,css,html}",
    webpackCliVersion := "4.10.0",
    libraryDependencies ++=
      Seq(
        "dev.optics" %%% "monocle-core" % "3.1.0",
        "dev.optics" %%% "monocle-macro" % "3.1.0",
        "org.scala-js" %%% "scalajs-dom" % "2.2.0",
        "com.github.japgolly.scalajs-react" %%% "core" % "2.1.1",
        "com.github.japgolly.scalajs-react" %%% "extra" % "2.1.1",
        "com.github.japgolly.scalacss" %%% "core" % "1.0.0",
        "com.github.japgolly.scalacss" %%% "ext-react" % "1.0.0"
      ),
    Compile / npmDependencies ++= Seq(
      "react" -> "18.2.0",
      "react-dom" -> "18.2.0"
    ),
    Compile / npmDevDependencies ++= Seq(
      "webpack-merge" -> "5.8.0",
      "html-webpack-plugin" -> "5.5.0",
      "sass-loader" -> "13.0.2",
      "css-loader" -> "6.7.1",
      "style-loader" -> "3.3.1"
    )
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
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
