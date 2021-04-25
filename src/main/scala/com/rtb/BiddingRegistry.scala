package com.rtb

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.StatusCodes
import com.rtb.dao.Payloads.{Banner, BidRequest, BidResponse, Campaign, HttpResponse, Targeting}

import java.util.UUID

/**
 * Igbalajobi Jamiu.
 */
object BiddingRegistry {

  /**
   * Actor-Typed Behaviour trait
   */
  sealed trait Command

  final case class GetBidding(userBidRequest: BidRequest, replyTo: ActorRef[HttpResponse[BidResponse]]) extends Command
  final case class GetCampaigns(replyTo: ActorRef[HttpResponse[Seq[Campaign]]]) extends Command

  def apply(): Behavior[Command] = Behaviors.receiveMessage {
    case GetCampaigns(replyTo) =>
      val campaigns = getCampainsFromDB
      replyTo ! (if(campaigns.isEmpty) HttpResponse(httpStatusCode = StatusCodes.NoContent.intValue, description = "No campaign at the moment. Please check back later.") else HttpResponse(httpStatusCode = StatusCodes.OK.intValue, description = "Request successful", data = Some(campaigns)))
      Behaviors.same
    case GetBidding(bidRequest, replyTo) =>
      /**
       * Get all the user bidding parameters that are required for matching
       */
      val (bidFloor, country, siteId, widths, heights) = (
        bidRequest.imp.map(_.flatMap(_.bidFloor)),
        bidRequest.user.flatMap(_.geo).flatMap(_.country),
        bidRequest.site.id,
        bidRequest.imp.map(_.flatMap(y => y.w.orElse(y.wmin).orElse(y.wmax))),
        bidRequest.imp.map(_.flatMap(y => y.h.orElse(y.hmin).orElse(y.hmax)))
      )

      /**
       * Perform logic to find a matching pair of campaign based on the user's input.
       */
      val data = getCampainsFromDB.find{ node =>
        true
      }.map(campaign => BidResponse(
        id = UUID.randomUUID(),
        bidRequestId = bidRequest.id,
        banner = campaign.banners.headOption,
        price = bidRequest.imp.map(_.flatMap(_.bidFloor)).map(_.sum).getOrElse(0),
        adid = Option(campaign.id.toString)
      ))

      replyTo ! HttpResponse(
        httpStatusCode = if(data.isDefined) StatusCodes.OK.intValue else StatusCodes.NoContent.intValue,
        description = if(data.isDefined) "Request Successful" else "No Content",
        data = data
      )
      Behaviors.same
  }

  /**
   * FAKE a Database.
   * Usually, this should come from the database or any storage system.
   * @return
   */
  private def getCampainsFromDB: Seq[Campaign] = Seq(
    Campaign(
      id = 1,
      country = "LT",
      targeting = Targeting(
        targetedSiteIds = List(UUID.randomUUID())
      ),
      banners = List(
        Banner(
          id = 1,
          src = "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
          width = 300,
          height = 250
        )
      ),
      bid = 5d
    )
  )

}
