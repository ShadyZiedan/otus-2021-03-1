package me.chuwy.otusfp.hw.services

import cats.effect.kernel.Async
import cats.effect.std.Dispatcher
import cats.effect.{Concurrent, IO, Ref, Sync, Temporal}
import fs2.Stream
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import cats.implicits._

import scala.concurrent.duration.DurationInt

object ApiService {

  trait Api[F[_]] {
    def counter(): F[Api.Counter]

    def getStream(chunk: Int, total: Int, time: Int): Stream[F, String]
  }

  object Api {
    final case class Counter(count: Int)

    final case class CounterEnv[F[_]](count: Ref[F, Int]) {
      def increment: F[Int] = this.count.updateAndGet(_ + 1)
    }

    object CounterEnv {
      def build[F[_] : Sync](): F[CounterEnv[F]] = for {
        ref <- Ref[F].of(0)
      } yield CounterEnv(ref)
    }


    object Counter {
      implicit val counterEncoder: Encoder[Counter] = new Encoder[Counter] {
        override def apply(counter: Counter): Json = Json.obj(
          ("count", Json.fromInt(counter.count))
        )
      }

      implicit def counterEntityEncoder[F[_]]: EntityEncoder[F, Counter] =
        jsonEncoderOf[F, Counter]
    }

    def impl[F[_] : Async](env: CounterEnv[F])(implicit timer: Temporal[F]): Api[F] = new Api[F] {
      override def counter() =
        for {
          count <- env.increment
        } yield Counter(count)

      override def getStream(chunk: Int, total: Int, time: Int): Stream[F, String] = {
        SlowStreamer.getStream[F](chunk, total, time)
      }

    }

    object SlowStreamer {

      def getStream[F[_]](chunk: Int, total: Int, time: Int)(implicit F: Concurrent[F], timer: Temporal[F]): Stream[F, String] =
        Stream.constant("a")
          .covary[F]
          .take(total.toLong)
          .chunkN(chunk)
          .map(c => c.toList.mkString)
          .evalMap(s => timer.sleep(time.second) *> F.pure(s))
    }
  }
}
