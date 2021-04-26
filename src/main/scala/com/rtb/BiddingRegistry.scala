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
      val campaigns = getCampaignsFromDB
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

        /**
         * At least one of w, wmin, wmax will be specified for width and at least one of h, hmin, hmax will be specified for height.
         * Always prefer validating w and h if they exist, otherwise fallback to wmin, wmax, hmin and hmax. min/max values might have different combinations
         */
        bidRequest.imp.map(_.flatMap(y => y.w.orElse(y.wmin).orElse(y.wmax))),
        bidRequest.imp.map(_.flatMap(y => y.h.orElse(y.hmin).orElse(y.hmax)))
      )

      /**
       * Perform logic to find a matching pair of campaign based on the user's input.
       */
      val data = getCampaignsFromDB.find{ node =>
        //Check against the siteIds
        node.targeting.targetedSiteIds.map(_.toString).exists(_.equalsIgnoreCase(siteId)) &&
        //Check the country if matched
        country.contains(node.country) &&
        //Optionally matching the width and height of a campaign banner
        (widths.exists(_.containsSlice(node.banners.map(_.width))) || heights.exists(_.containsSlice(node.banners.map(_.height))))
        //

      }.map(campaign => BidResponse(
        id = UUID.randomUUID(), //Generate a bidResponseId with UUID for unique references
        bidRequestId = bidRequest.id,
        banner = campaign.banners.headOption,
        price = bidRequest.imp.map(_.flatMap(_.bidFloor)).map(_.sum).getOrElse(0),
        // adid is the campaign ID of the campaign selected
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
  private def getCampaignsFromDB: Seq[Campaign] = Seq(
    Campaign(
      id = 1,
      country = "LT",
      targeting = Targeting(
        targetedSiteIds = List(UUID.fromString("5fdb2b0f-00f8-44b7-9c6a-04ad66d9326a"))
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
