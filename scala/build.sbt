ThisBuild / scalaVersion := "3.1.3"
lazy val library =
  project
    .in(file("library"))
    .settings(
      version := "0.0.1-SNAPSHOT",
      libraryDependencies ++=
        Seq("org.scalatest" %% "scalatest" % "3.2.13" % Test),
      Compile / unmanagedSourceDirectories += baseDirectory.value / "gen"
    )

lazy val interpreter =
  project
    .in(file("interpreter"))
    .settings(
      version := "0.0.1-SNAPSHOT",
      libraryDependencies ++=
        Seq("org.jline" % "jline-reader" % "3.21.0", "org.scalatest" %% "scalatest" % "3.2.13" % Test)
    )
    .dependsOn(library)

lazy val ideaPlugin =
  project
    .in(file("ideaPlugin"))
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      version := "0.0.1-SNAPSHOT",
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
//      Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
//      Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main" / "java",
//      Compile / unmanagedSourceDirectories += baseDirectory.value / "gen",
//      Test / unmanagedResourceDirectories += baseDirectory.value / "testResources"
      //      packageLibraryMappings += "org.typelevel" %% "cats*" % ".*" -> Some("lib/cats.jar"),
    )
