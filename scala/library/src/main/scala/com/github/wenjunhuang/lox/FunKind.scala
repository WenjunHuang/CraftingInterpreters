package com.github.wenjunhuang.lox

enum FunKind:
  case Function
  case Method

  override def toString: String =
    this match
      case Function => "function"
      case Method   => "method"
