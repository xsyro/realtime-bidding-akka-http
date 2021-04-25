package com.rtb.dao

import java.util.UUID

/**
 * Igbalajobi Jamiu
 */
object Payloads {

  /**
   * Structure the response payload in a uniform format.
   * @param httpStatusCode - This would also be equivalent to HTTP header status code.
   * @param description
   * @param data
   * @tparam T
   */
  case class HttpResponse[T](httpStatusCode: Int, description: String, data: Option[T] = None)


  /**
   * Http request-body for getting bid request.
   */
  case class Impression(id: String, wmin: Option[Int], wmax: Option[Int], w: Option[Int], hmin: Option[Int], hmax: Option[Int], h: Option[Int], bidFloor: Option[Double])
  case class Site(id: String, domain: String)
  case class User(id: String, geo: Option[Geo])
  case class Device(id: String, geo: Option[Geo])
  case class Geo(country: Option[String])
  case class BidRequest(id: String, site: Site, imp: Option[List[Impression]] = None, user: Option[User] = None, device: Option[Device] = None)

  /**
   * Http response-body for providing payload
   */
  case class BidResponse(id: UUID, bidRequestId: String,  price: Double, adid: Option[String], banner: Option[Banner])


  case class Campaign(id: Int, country: String, targeting: Targeting, banners: List[Banner], bid: Double)
  case class Targeting(targetedSiteIds: List[UUID]) //UUID
  case class Banner(id: Int, src: String, width: Int, height: Int)
}
