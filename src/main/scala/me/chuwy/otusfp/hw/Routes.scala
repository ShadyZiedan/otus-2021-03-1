package me.chuwy.otusfp.hw

import cats.effect.kernel.Temporal
import me.chuwy.otusfp.hw.services.ApiService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import cats.implicits._

object Routes {
  def apiRoutes[F[_] : Temporal](api: ApiService.Api[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "api" / "count" => {
        import ApiService.Api.Counter._
        for {
          count <- api.counter()
          resp <- Ok(count)
        } yield resp
      }
      case GET -> Root / "api" / "slow" / IntVar(chunk) / IntVar(total) / IntVar(time) =>
        Ok(api.getStream(chunk, total, time))
    }
  }
}
