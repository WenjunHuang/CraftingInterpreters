package com.github.wenjunhuang.lox

enum FunKind:
  case Function
  case Initializer
  case Method

  override def toString: String =
    this match
      case Function => "function"
      case Method   => "method"
      case Initializer => "initializer"
