package com.rtb

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.rtb.BiddingRegistry.{GetBidding, GetCampaigns}
import com.rtb.dao.Payloads.{BidRequest, BidResponse, Campaign, HttpResponse}

import scala.concurrent.Future

/**
 * Igbalajobi Jamiu
 *
 * @param biddingRegistry
 * @param actorSystem
 */
class BiddingRoutes(biddingRegistry: ActorRef[BiddingRegistry.Command])(implicit val actorSystem: ActorSystem[_]) {

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(actorSystem.settings.config.getDuration("my-app.routes.ask-timeout"))

  //#import-json-formats

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  val routes: Route = {
    pathPrefix("bidding") {
      concat(
        // #/v1/api/campaigns
        pathPrefix("campaigns") {
          get {
            complete(getCampaigns)
          }
        },
        pathPrefix("bid-for-campaign") {
          entity(as[BidRequest]) { userBidRequest =>
            onSuccess(bidForCampaign(userBidRequest)) { actionCompleted =>
              complete(actionCompleted)
            }
          }
        }
      )
    }
  }

  def getCampaigns: Future[HttpResponse[Seq[Campaign]]] =
    biddingRegistry.ask(GetCampaigns)

  def bidForCampaign(bidRequest: BidRequest): Future[HttpResponse[BidResponse]] =
    biddingRegistry.ask(GetBidding(bidRequest, _))
}
