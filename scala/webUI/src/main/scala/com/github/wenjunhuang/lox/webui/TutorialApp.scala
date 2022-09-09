package com.github.wenjunhuang.lox.webui

import com.github.wenjunhuang.lox.*
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, ReactEventFromInput, ScalaComponent}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSImport}

@js.native
@JSImport("./styles.scss", JSImport.Namespace)
object Css extends js.Object:
end Css

case class User(id: Int, name: String)

object TutorialApp:
  val css: Css.type = Css

  def onTextChange(e: ReactEventFromInput): Callback =
    Callback.log(s"Value received = ${e.target.value}")

  val loggedInUser: Option[User]      = Some(User(1, "huangwenjun"))
  val hasGreen                        = true
  def main(args: Array[String]): Unit =
    ScalaComponent
      .builder[Unit]
      .renderStatic(
        <.div(
          <.h3(
            (^.color := "green").when(hasGreen),
            "Welcome"
          ),
          loggedInUser.whenDefined(user =>
            TagMod(
              ^.cls := "user-logged-in",
              <.a(^.href := user.name, "My Profile")
            )
          )
        )
      )
      .build()
      .renderIntoDOM(dom.document.getElementById("root"))
end TutorialApp
