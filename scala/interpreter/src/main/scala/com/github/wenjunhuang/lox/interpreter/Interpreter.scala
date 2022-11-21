package com.github.wenjunhuang.lox.interpreter

import com.github.wenjunhuang.lox.*

import java.io.PrintStream
import scala.collection.mutable

class Interpreter(output: PrintStream) extends ExprVisitor with StatementVisitor:
  import TokenType.*
  import Value.*

  private var environment = Environment.Global
  private var locals      = Map.empty[Expression, Int]

  Environment.Global.define(
    "clock",
    Value.FunctionValue(0,
                        params =>
                          val result = System.currentTimeMillis() / 1000.0
                          NumericValue(result)
    )
  )
  def interpret(statements: Seq[Statement]): Unit =
    try for stm <- statements do execute(stm)
    catch case error: RuntimeError => Lox.runtimeError(error)

  private def findClassMember(classValue: ClassValue, name: String, inst: InstanceValue): Option[Value] =
    classValue.methods.get(name)
      .map(method => Value.FunctionValue(method.arity, params => method.body(inst, params)))
      .orElse(inst.fields.get(name))

  override def visitGet(expr: Expression.Get): Value =

    val obj = evaluate(expr.obj)
    obj match
      case inst @ Value.InstanceValue(myClass, _) =>
        findClassMember(myClass, expr.name.lexeme, inst)
          .orElse(myClass.superClass.flatMap(findClassMember(_, expr.name.lexeme, inst)))
          .getOrElse(throw RuntimeError(expr.name, s"Undefined property '${expr.name.lexeme}'."))

      case _ =>
        throw new RuntimeError(expr.name, "Only instances have properties.")

  override def visitLiteral(expr: Expression.Literal): Value = expr.value

  override def visitUnary(expr: Expression.Unary): Value =
    val right = evaluate(expr.right)
    expr.operator.tt match
      case TokenType.MINUS =>
        checkNumberOperand(expr.operator, right, it => NumericValue(-it))
      case TokenType.BANG  =>
        BooleanValue(!isTruthy(right))
      case _               =>
        NoValue

  override def visitBinaryExpr(expr: Expression.Binary): Value =
    val left  = evaluate(expr.left)
    val right = evaluate(expr.right)
    expr.operator.tt match
      case TokenType.MINUS =>
        checkNumberOperands(expr.operator, left, right, (a, b) => NumericValue(a - b))
      case TokenType.PLUS  =>
        (left, right) match
          case (NumericValue(l), NumericValue(r)) => NumericValue(l + r)
          case (StringValue(l), StringValue(r))   => StringValue(l + r)
          case _                                  => NoValue
      case TokenType.SLASH =>
        checkNumberOperands(expr.operator, left, right, (a, b) => NumericValue(a / b))
      case TokenType.STAR  =>
        checkNumberOperands(expr.operator, left, right, (a, b) => NumericValue(a * b))
      case GREATER         =>
        checkNumberOperands(expr.operator, left, right, (a, b) => BooleanValue(a > b))
      case GREATER_EQUAL   =>
        checkNumberOperands(expr.operator, left, right, (a, b) => BooleanValue(a >= b))
      case LESS            =>
        checkNumberOperands(expr.operator, left, right, (a, b) => BooleanValue(a < b))
      case LESS_EQUAL      =>
        checkNumberOperands(expr.operator, left, right, (a, b) => BooleanValue(a <= b))
      case BANG_EQUAL      =>
        BooleanValue(left != right)
      case EQUAL_EQUAL     =>
        BooleanValue(left == right)
      case _               =>
        NoValue

  override def visitGroupExpr(expr: Expression.Grouping): Value =
    evaluate(expr.expression)

  private def evaluate(expr: Expression) = expr.accept(this)

  private def isTruthy(value: Value): Boolean =
    value match
      case BooleanValue(v) => v
      case NoValue         => false
      case _               => true

  private def checkNumberOperand[T](operator: Token, operand: Value, func: Double => T): T =
    operand match
      case NumericValue(v: Double) => func(v)
      case _                       => throw new RuntimeError(operator, "Operand must be number.")

  private def checkNumberOperands[T](operator: Token, left: Value, right: Value, func: (Double, Double) => T): T =
    (left, right) match
      case (NumericValue(l), NumericValue(r)) => func(l, r)
      case _                                  => throw new RuntimeError(operator, "Operands must be numbers.")

  override def visitExpressionStatement(statement: Statement.Expr): Unit =
    evaluate(statement.expression)
    ()

  override def visitPrintStatement(statement: Statement.Print): Unit =
    val value = evaluate(statement.expression)
    output.println(value)
    ()

  override def visitVariable(expr: Expression.Variable): Value =
    lookUpVariable(expr.name, expr).getOrElse(Value.NoValue)

  override def visitVarStatement(statement: Statement.Var): Unit =
    val value = statement.initializer match
      case Some(initializer) =>
        evaluate(initializer)
      case None              => NoValue

    environment.define(statement.name.lexeme, value)

  override def visitClassStatement(statement: Statement.Class): Unit =
    val superClassValue =
      for superClass <- statement.superClass
      yield evaluate(superClass) match
        case cv: ClassValue => cv
        case _              => throw RuntimeError(superClass.name, "Superclass must be a class.")

    val currentEnvironment = superClassValue.map { suerClass =>
      val superEnv = Environment(environment)
      superEnv.define("super", suerClass)
      superEnv
    }.getOrElse(environment)

    val initializers: Vector[Value.InitializerValue] = statement.initializers.map { func =>
      Value.InitializerValue(
        func.params.length,
        { (instance, params) =>
          val thisEnv   = Environment(currentEnvironment)
          thisEnv.define("this", instance)
          val paramsEnv = Environment(thisEnv)
          func.params.zip(params).foreach { case (param, value) => paramsEnv.define(param.lexeme, value) }
          try
            executeBlock(func.body.statements, Environment(paramsEnv))
            instance
          catch
            case _: Return =>
              instance
        }
      )
    }
    val methods: Map[String, Value.MethodValue]      = statement.methods.map[(String, Value.MethodValue)] { func =>
      (func.name.lexeme,
       Value.MethodValue(
         func.params.length,
         {
           case (instance, params) =>
             val thisEnv   = Environment(currentEnvironment)
             thisEnv.define("this", instance)
             val paramsEnv = Environment(thisEnv)
             func.params.zip(params).foreach { case (param, value) => paramsEnv.define(param.lexeme, value) }
             try
               executeBlock(func.body.statements, Environment(paramsEnv))
               Value.NoValue
             catch
               case returnValue: Return =>
                 returnValue.value
         }
       )
      )
    }.toMap
    environment.define(statement.name.lexeme, ClassValue(statement.name.lexeme, superClassValue, initializers, methods))

  override def visitAssignment(expr: Expression.Assign): Value =
    val value = evaluate(expr.value)
    locals.get(expr) match
      case Some(distance) => environment.assignAt(distance, expr.name, value)
      case _              => Environment.Global.assign(expr.name, value)
    value

  override def visitBlockStatement(statement: Statement.Block): Unit =
    executeBlock(statement.statements, Environment(environment))

  def executeBlock(statements: Vector[Statement], environment: Environment): Unit =
    val previous = this.environment
    try
      this.environment = environment
      for statement <- statements do execute(statement)
    finally this.environment = previous

  override def visitIfStatement(statement: Statement.If): Unit =
    if isTruthy(evaluate(statement.condition)) then execute(statement.thenBranch)
    else
      statement.elseBranch match
        case Some(stmt) => execute(stmt)
        case None       =>

  private def execute(statement: Statement): Unit = statement.accept(this)

  override def visitLogical(expr: Expression.Logical): Value =
    val left = evaluate(expr.left)
    expr.operator.tt match
      case TokenType.OR  =>
        if isTruthy(left) then left else evaluate(expr.right)
      case TokenType.AND =>
        if isTruthy(left) then evaluate(expr.right) else left
      case _             =>
        Lox.runtimeError(new RuntimeError(expr.operator, "Invalid logical operator."))
        NoValue

  override def visitWhileStatement(statement: Statement.While): Unit =
    while isTruthy(evaluate(statement.condition)) do execute(statement.body)

  override def visitCall(expr: Expression.Call): Value =
    val callee = evaluate(expr.callee)

    callee match
      case Value.FunctionValue(_, body)                                   =>
        val arguments = expr.arguments.map(evaluate)
        body(arguments)
      case cv @ Value.ClassValue(name, superClass, initializers, methods) =>
        val arity = expr.arguments.length
        // find initializer with equal arity count
        initializers.find(_.arity == arity) match
          case Some(initializer)                          =>
            val arguments                          = expr.arguments.map(evaluate)
            val instanceValue: Value.InstanceValue = Value.InstanceValue(cv, mutable.Map.empty)
            initializer.body(instanceValue, arguments)
          case None if initializers.isEmpty && arity == 0 =>
            Value.InstanceValue(cv, mutable.Map.empty)
          case _                                          =>
            Lox.runtimeError(new RuntimeError(expr.paren, s"Can not find initializer with given arity of $arity"))
            NoValue
      case _                                                              =>
        Lox.runtimeError(new RuntimeError(expr.paren, "Can only call functions or classes."))
        NoValue

  override def visitFunctionStatement(statement: Statement.Func): Unit =
    val currentEnvironment = environment
    currentEnvironment.define(
      statement.name.lexeme,
      Value.FunctionValue(
        statement.params.size,
        { params =>
          val paramsEnv = Environment(currentEnvironment)
          statement.params.zip(params).foreach { case (param, value) => paramsEnv.define(param.lexeme, value) }
          try
            executeBlock(statement.body.statements, Environment(paramsEnv))
            NoValue
          catch
            case returnValue: Return =>
              returnValue.value
        }
      )
    )

  override def visitReturnStatement(statement: Statement.Return): Unit =
    val value = statement.expression match
      case Some(v) => evaluate(v)
      case None    => NoValue
    throw Return(value)

  def resolve(expression: Expression, depth: Int): Unit =
    locals = locals + (expression -> depth)

  private def lookUpVariable(name: Token, expr: Expression) =
    locals.get(expr) match
      case Some(depth) => environment.getAt(name, depth)
      case None        => Some(Environment.Global.get(name))

  override def visitSet(expr: Expression.Set): Value =
    val obj = evaluate(expr.obj)
    obj match
      case Value.InstanceValue(_, fields) =>
        val value = evaluate(expr.value)
        fields.put(expr.name.lexeme, value)
        value
      case _                              =>
        throw RuntimeError(expr.name, "Only instances have fields.")

  override def visitThis(expr: Expression.This): Value =
    lookUpVariable(expr.keyword, expr) match
      case Some(value) => value
      case None        => throw new RuntimeError(expr.keyword, "Can not use 'this' outside of a class.")

  override def visitSuper(expr: Expression.Super): Value =
    lookUpVariable(expr.keyword, expr) match
      case Some(value: ClassValue) =>
        val inst = environment.get("this").asInstanceOf[Value.InstanceValue]
        findClassMember(value, expr.method.lexeme, inst)
          .orElse(value.superClass.flatMap(findClassMember(_, expr.method.lexeme, inst)))
          .getOrElse(throw Exception(s"Undefined property '${expr.method.lexeme}'."))
      case _                       => throw new RuntimeError(expr.keyword, "Can not use 'super' outside of a class.")

end Interpreter
