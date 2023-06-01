val versions = new {
  val scalaVersion  = "3.2.1"
  val scalaTest     = "3.2.15"
  val monocle       = "3.1.0"
  val scalaJSDom    = "2.2.0"
  val scalaJSReact  = "2.1.1"
  val scalaCss      = "1.0.0"
  val react         = "18.2.0"
  val intelliJBuild = "2022.2.1"

  val scalaSwing = "3.0.0"
  val catsEffect = "3.3.14"
  val jline      = "3.21.0"
}

ThisBuild / scalaVersion := versions.scalaVersion

lazy val macros  = project
//lazy val library =
//  crossProject(JSPlatform, JVMPlatform)
//    .crossType(CrossType.Pure)
//    .in(file("library"))
//    .jvmSettings(
//      libraryDependencies ++=
//        Seq("org.scalatest"     %% "scalatest"     % versions.scalaTest % Test,
//            "org.apache.commons" % "commons-lang3" % "3.12.0"           % Test,
//            "commons-io"         % "commons-io"    % "2.11.0"           % Test
//        )
//    )
lazy val library = project.settings(
  libraryDependencies ++=
    Seq("org.scalatest"     %% "scalatest"     % versions.scalaTest % Test,
        "org.apache.commons" % "commons-lang3" % "3.12.0"           % Test,
        "commons-io"         % "commons-io"    % "2.11.0"           % Test
    )
)

lazy val interpreter =
  project
    .in(file("interpreter"))
    .settings(
      libraryDependencies ++=
        Seq(
          "org.jline"          % "jline-reader"  % versions.jline,
          "org.scalatest"     %% "scalatest"     % versions.scalaTest % Test,
          "org.apache.commons" % "commons-lang3" % "3.12.0"           % Test,
          "commons-io"         % "commons-io"    % "2.11.0"           % Test
        )
    )
    .dependsOn(library)

//lazy val webUI = project
//  .in(file("webUI"))
//  .settings(
//    scalaJSUseMainModuleInitializer       := true,
//    fastOptJS / webpackBundlingMode       := BundlingMode.LibraryOnly("LoxWebUI"),
//    fastOptJS / webpackConfigFile         := Some(baseDirectory.value / "dev.webpack.config.js"),
//    fastOptJS / webpackDevServerExtraArgs ++= Seq(
//      "--static-directory",
//      (Compile / fastOptJS / artifactPath).value.getParentFile.getAbsolutePath
//    ),
//    webpack / webpackDevServerPort        := 3000,
//    webpackResources                      := webpackResources.value +++ ((Compile / resourceDirectory).value * ("*.scss" || "*.html")),
//    webpack / version                     := "5.74.0",
//    startWebpackDevServer / version       := "4.10.0",
//    webpackCliVersion                     := "4.10.0",
//    webpackMonitoredFiles / includeFilter := "**/*.{js,css,html}",
//    libraryDependencies ++=
//      Seq(
//        "dev.optics"                        %%% "monocle-core"  % versions.monocle,
//        "dev.optics"                        %%% "monocle-macro" % versions.monocle,
//        "org.scala-js"                      %%% "scalajs-dom"   % versions.scalaJSDom,
//        "com.github.japgolly.scalajs-react" %%% "core"          % versions.scalaJSReact,
//        "com.github.japgolly.scalajs-react" %%% "extra"         % versions.scalaJSReact,
//        "com.github.japgolly.scalacss"      %%% "core"          % versions.scalaCss,
//        "com.github.japgolly.scalacss"      %%% "ext-react"     % versions.scalaCss
//      ),
//    Compile / npmDependencies ++= Seq(
//      "react"     -> versions.react,
//      "react-dom" -> versions.react
//    ),
//    Compile / npmDevDependencies ++= Seq(
//      "webpack-merge"       -> "5.8.0",
//      "html-webpack-plugin" -> "5.5.0",
//      "sass-loader"         -> "13.0.2",
//      "css-loader"          -> "6.7.1",
//      "style-loader"        -> "3.3.1"
//    )
//  )
//  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
//  .dependsOn(library.js)

ThisBuild / packageMethod      := PackagingMethod.Skip()
ThisBuild / intellijPluginName := "CraftingInterpreters"
ThisBuild / intellijBuild      := versions.intelliJBuild
ThisBuild / intellijPlatform   := IntelliJPlatform.IdeaCommunity

lazy val deletePluginJar = taskKey[Unit]("Delete package jar")
lazy val ideaPlugin      =
  project
    .in(file("ideaPlugin"))
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      packageMethod                  := PackagingMethod.Standalone(),
      Global / intellijAttachSources := true,
      deletePluginJar                := Def.task {
        val file = packageOutputDir.value
        if (file.exists()) {
          IO.delete(file)
        }
      }.value,
      intellijPlugins ++= Seq(
        "org.intellij.intelliLang".toPlugin,
        "com.intellij.platform.images".toPlugin,
        "PsiViewer:223-SNAPSHOT".toPlugin
      ),
      intellijVMOptions              := intellijVMOptions.value.copy(
        defaultOptions = intellijVMOptions.value.defaultOptions.filterNot(_.contains("idea.debug.mode"))
      ),
      packageLibraryMappings         := Seq.empty, // allow scala-library
      Compile / unmanagedSourceDirectories += (baseDirectory.value / "gen"),
      libraryDependencies ++=
        Seq(
          "org.scala-lang" % "scala3-library_3" % versions.scalaVersion,
          "org.scalatest" %% "scalatest"        % versions.scalaTest % Test
        ),
      library / packageMethod        := PackagingMethod.MergeIntoParent(),
      interpreter / packageMethod    := PackagingMethod.MergeIntoParent()
    )
    .dependsOn(library, interpreter, macros)
