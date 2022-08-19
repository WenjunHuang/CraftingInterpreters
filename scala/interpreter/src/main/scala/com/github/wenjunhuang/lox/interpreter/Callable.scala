package com.github.wenjunhuang.lox.interpreter

import com.github.wenjunhuang.lox.Value

trait Callable:
  def arity: Int
  def apply(interpreter: Interpreter, arguments: List[Value]): Value
end Callable

