package me.chuwy.otusfp.intro

import scala.concurrent.Future
import scala.io.StdIn

object IOIntro {
  object Constructors {
    import cats.effect.IO

    // 1. Чистое значение
    val a: IO[Int] = IO.pure(42)

    // 2. Сайд-эффект
    def side = IO.delay {
      val in = StdIn.readLine()
      println(in)
    }

    // 3. Умные конструкторы
    def f: Future[Int] = ???
    def either = IO.fromFuture(IO.delay(f))
  }


  object Combinators {
    // 1. Последовательное вычисление
    def double = {
      Constructors.side *>
        Constructors.side
    }
  }
}
