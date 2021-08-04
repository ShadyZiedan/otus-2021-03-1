package me.chuwy.otusfp

import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import io.circe.syntax.EncoderOps
import me.chuwy.otusfp.hw.Routes
import me.chuwy.otusfp.hw.services.ApiService.Api
import me.chuwy.otusfp.hw.services.ApiService.Api.Counter
import org.http4s._
import org.http4s.implicits._
import org.specs2.mutable.Specification

import scala.concurrent.duration.{Duration, DurationInt}
import Counter._


class HWApiSpec extends Specification with CatsEffect {
  override protected val Timeout: Duration = 30.seconds
  private val apiInstance: IO[Api[IO]] = {
    for {
      env <- Api.CounterEnv.build[IO]()
    } yield Api.impl(env)
  }

  "Counter service" should {
    "get a json response" in {
      val req = Request[IO](Method.GET, uri"/api/count")

      val response: IO[Response[IO]] = for {
        api <- apiInstance
        res <- Routes.apiRoutes(api).orNotFound(req)
      } yield res

      response.flatMap(_.as[String]).map(str => str must beEqualTo(Counter(1).asJson.noSpaces))
    }

    "increment" in {
      val req = Request[IO](Method.GET, uri"/api/count")

      val response: IO[Response[IO]] = for {
        api <- apiInstance
        _ <- Routes.apiRoutes(api).orNotFound(req)
        res <- Routes.apiRoutes(api).orNotFound(req)
      } yield res

      response.flatMap(_.as[String]).map(str => str must beEqualTo(Counter(2).asJson.noSpaces))
    }
  }


  "Slow streamer" should {
    "get correct stream length" in {
      val req = Request[IO](Method.GET, uri"/api/slow/100/1024/1")

      val response = for {
        api <- apiInstance
        res <- Routes.apiRoutes(api).orNotFound(req)
      } yield res

      response.flatMap(_.as[String]).map(str => str must beEqualTo("a" * 1024))
    }
  }


}
