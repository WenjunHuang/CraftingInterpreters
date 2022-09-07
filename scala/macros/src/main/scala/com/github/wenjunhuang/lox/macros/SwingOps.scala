package com.github.wenjunhuang.lox.macros

import java.awt.Component
import scala.quoted.*

object SwingOps:
  inline def add[T](inline component: Component, inline constraints: Any)(using
      contextWrapper: ContextWrapper[T]
  ): Unit =
    ${ addImpl('contextWrapper, 'component, 'constraints) }
  private def addImpl[T: Type](context: Expr[ContextWrapper[T]], component: Expr[Component], constraints: Expr[Any])(
      using quotes: Quotes
  ): Expr[Unit] =
    import quotes.reflect.*
    val componentTree = component.asTerm
    val constraintsTree = constraints.asTerm
    Select
      .overloaded(
        context.asTerm,
        "add",
        Nil,
        List(componentTree, constraintsTree)
      ).asExprOf[Unit]

end SwingOps
