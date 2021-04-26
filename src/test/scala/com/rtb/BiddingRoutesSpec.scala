package com.rtb

import _root_.com.rtb.dao.Payloads.{BidRequest, BidResponse}
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json.JsonParser
import com.rtb.dao.Payloads.{HttpResponse => DaoHttpResponse}

/**
 * Igbalajobi Jamiu.
 */
class BiddingRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  // the Akka HTTP route testkit does not yet support a typed actor system
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe
  // created with testKit.createTestProbe()
  val biddingRegistry = testKit.spawn(BiddingRegistry())
  lazy val routes = new BiddingRoutes(biddingRegistry).routes

  // use the json formats to marshal and unmarshall objects in the test

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  "BiddingRoutes" should {
    "return no campaigns if no present (GET /bidding/campaigns)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/bidding/campaigns")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"data":[{"banners":[{"height":250,"id":1,"src":"https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg","width":300}],"bid":5.0,"country":"LT","id":1,"targeting":{"targetedSiteIds":["5fdb2b0f-00f8-44b7-9c6a-04ad66d9326a"]}}],"description":"Request successful","httpStatusCode":200}""")
      }
    }

    //#testing-bidding
    "be able to bid for campaign (POST /bidding/bid-for-campaign)" in {
      val bidRequest = JsonParser("""{"id":"SGu1Jpq1IO","site":{"id":"5fdb2b0f-00f8-44b7-9c6a-04ad66d9326a","domain":"fake.tld"},"device":{"id":"440579f4b408831516ebd02f6e1c31b4","geo":{"country":"LT"}},"imp":[{"id":"1","wmin":50,"wmax":300,"hmin":100,"hmax":300,"h":250,"w":300,"bidFloor":3.12123}],"user":{"geo":{"country":"LT"},"id":"USARIO1"}}""").convertTo[BidRequest]
      val bidRequestEntity = Marshal(bidRequest).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/bidding/bid-for-campaign").withEntity(bidRequestEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        /**
         * This value is a dynamic generated value. If not retrieved, the test case wouldn't pass.
         */
        val dynamicGeneratedBidId = responseAs[DaoHttpResponse[BidResponse]].data.get.id.toString
        // and we know what message we're expecting back:
        entityAs[String] should ===(s"""{"data":{"adid":"1","banner":{"height":250,"id":1,"src":"https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg","width":300},"bidRequestId":"SGu1Jpq1IO","id":"${dynamicGeneratedBidId}","price":3.12123},"description":"Request Successful","httpStatusCode":200}""")
      }
    }
  }

}
