package me.chuwy.otusfp.hw

import cats.effect.kernel.{Async, Temporal}
import cats.effect.std.Dispatcher
import cats.effect.{IO, Sync}
import fs2.Stream
import me.chuwy.otusfp.hw.services.ApiService
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object Server {

  def stream[F[_]: Async]: Stream[F, Nothing] = {
    for {
      env <- Stream.eval(ApiService.Api.CounterEnv.build[F]())
      apiAlg = ApiService.Api.impl[F](env)


      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = Routes.apiRoutes[F](apiAlg).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain

}
