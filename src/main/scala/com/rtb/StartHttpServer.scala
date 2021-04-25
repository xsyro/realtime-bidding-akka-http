package com.rtb

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.{Http, ServerBuilder}
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

//#main-class
object StartHttpServer extends App {
  //#start-http-server
  private def startHttpServer(routes: Route*)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding: ServerBuilder = Http().newServerAt("localhost", system.settings.config.getInt("my-app.http.port"))
    Future.sequence(routes.map(x => futureBinding.bind(x))).onComplete {
      case Success(binding) =>
        val address = binding.head.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  //#start-http-server
  val rootBehavior = Behaviors.setup[Nothing] { context =>
    val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
    context.watch(userRegistryActor)

    val biddingRegistryActor = context.spawn(BiddingRegistry(), "BiddingRegistryActor")
    context.watch(biddingRegistryActor)

    val userRoutes = new UserRoutes(userRegistryActor)(context.system)
    val biddingRoutes = new BiddingRoutes(biddingRegistryActor)(context.system)


    startHttpServer(userRoutes.userRoutes, biddingRoutes.biddingRoutes)(context.system)
    Behaviors.empty
  }
  ActorSystem[Nothing](rootBehavior, "RealTimeBiddingAkkaHttp")
  //#server-bootstrapping
}
//#main-class
