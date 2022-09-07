package com.github.wenjunhuang.lox.macros
import scala.quoted.*

object PojoBuilder:

  inline def pojoBuilder[T <: AnyRef](inline init: ContextWrapper[T] ?=> Unit): T = ${ builderImpl[T]('{ init }) }
  private def builderImpl[T <: AnyRef: Type](init: Expr[ContextWrapper[T] ?=> Unit])(using quotes: Quotes): Expr[T] =
    import quotes.reflect.*
    val aTpr = TypeRepr.of[T]
    val ctor = aTpr.typeSymbol.declarations
      .filter(_.isClassConstructor)
      .find(_.paramSymss match
      case tps :: Nil :: Nil => true // has type parameters
      case Nil :: Nil        => true // not type parameters
      case _                 => false
      )
      .getOrElse(report.errorAndAbort("Class must has a no args constructor"))
    val expr = aTpr match
    case AppliedType(typcon, tyargs) =>
      New(Inferred(typcon)).select(ctor).appliedToTypes(tyargs).appliedToNone.asExprOf[T]
    case _ =>
      New(Inferred(aTpr)).select(ctor).appliedToNone.asExprOf[T]
    '{ val rtn = ${ expr }; $init(using rtn); rtn }

  extension (inline self: String)
    inline def :=[T <: AnyRef, V](inline value: V)(using t: ContextWrapper[T]): T = ${
      assignImpl[T, V]('self, 'value, 't)
    }
  private def assignImpl[T <: AnyRef: Type, V: Type](name: Expr[String], value: Expr[V], t: Expr[ContextWrapper[T]])(
      using quotes: Quotes
  ): Expr[T] =
    import quotes.reflect.*
    val setterName = s"set${name.valueOrAbort.capitalize}"
    val field = TypeRepr
      .of[T]
      .classSymbol
      .flatMap(_.memberMethod(setterName).headOption)
      .getOrElse(report.errorAndAbort(s"Class has no setter named $setterName"))
    val term = t.asTerm
    val assign = Select(term, field).appliedTo(value.asTerm).asExprOf[Unit]
    '{ $assign; $t }
end PojoBuilder
