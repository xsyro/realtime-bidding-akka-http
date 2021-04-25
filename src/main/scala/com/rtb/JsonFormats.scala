package com.rtb

import com.rtb.UserRegistry.ActionPerformed
import com.rtb.dao.Payloads
import com.rtb.dao.Payloads.{Banner, BidRequest, BidResponse, Campaign, Device, Geo, Impression, Site, Targeting, User => BidUser}
import spray.json.{JsString, JsValue, JsonFormat}

import java.util.UUID

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats {
  // import the default encoders for primitive types (Int, String, Lists etc)

  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)


  /**
   * The order of inter-dependent of class is important to the json formatter. Otherwise, this would result to compile error
   */
  implicit object UUIDJsonFormat extends JsonFormat[UUID] {
    override def write(obj: UUID): JsValue = JsString(obj.toString)
    override def read(json: JsValue): UUID = UUID.fromString(json.toString())
  }
  implicit val bannerResponseJsonFormat = jsonFormat4(Banner)
  implicit val geoJsonFormat = jsonFormat1(Geo)
  implicit val bidUserJsonFormat = jsonFormat2(BidUser)
  implicit val deviceJsonFormat = jsonFormat2(Device)
  implicit val siteJsonFormat = jsonFormat2(Site)
  implicit val impressionJsonFormat = jsonFormat8(Impression)


  implicit val userBidRequestJsonFormat = jsonFormat5(BidRequest)
  implicit val userBidResponseJsonFormat = jsonFormat5(BidResponse)

  implicit val targetingResponseJsonFormat = jsonFormat1(Targeting)
  implicit val campaignResponseJsonFormat = jsonFormat5(Campaign)


  implicit val httpResponseJsonFormatForCampaigns = jsonFormat3(Payloads.HttpResponse[Seq[Campaign]])
  implicit val httpResponseJsonFormatForAny = jsonFormat3(Payloads.HttpResponse[BidResponse])

}
//#json-formats
