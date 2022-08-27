package com.github.wenjunhuang.lox

import scala.util.control.NoStackTrace

class Return(val value: Value) extends Exception with NoStackTrace:

end Return
