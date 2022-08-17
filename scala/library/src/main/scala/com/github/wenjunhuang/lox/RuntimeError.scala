package com.github.wenjunhuang.lox

class RuntimeError(val token: Token, message: String) extends Exception(message)
end RuntimeError
