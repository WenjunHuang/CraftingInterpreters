package com.github.wenjunhuang.lox.webui
import com.github.wenjunhuang.lox.*

object TutorialApp:
  def main(args: Array[String]): Unit =
    val source =
      """
        |fun makeCounter(){
        |var i = 0;
        |fun count(){
        |i = i + 1;
        |print i;
        |}
        |return count;
        |}
        |
        |var counter = makeCounter();
        |counter();
        |counter();
        |
        |""".stripMargin
    val scanner = new Scanner(source)
