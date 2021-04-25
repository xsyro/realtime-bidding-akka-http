package com.rtb

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.{Failure, Success}

/**
 * Igbalajobi Jamiu.
 */
object StartHttpServer extends App {
  //#start-http-server
  private def startHttpServer(routes: Route*)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", system.settings.config.getInt("my-app.http.port"))
    futureBinding.bind(routes.head).onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  //#start-http-server
  val rootBehavior = Behaviors.setup[Nothing] { context =>

    val biddingRegistryActor = context.spawn(BiddingRegistry(), "BiddingRegistryActor")
    context.watch(biddingRegistryActor)

    val biddingRoutes = new BiddingRoutes(biddingRegistryActor)(context.system)


    startHttpServer(biddingRoutes.routes)(context.system)
    Behaviors.empty
  }
  ActorSystem[Nothing](rootBehavior, "RealTimeBiddingAkkaHttp")
}
