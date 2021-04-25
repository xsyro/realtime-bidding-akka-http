package com.rtb

//#test-top

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.rtb.dao.Payloads.{BidRequest, Site}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

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

  "UserRoutes" should {
    "return no campaigns if no present (GET /users)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/bidding/campaigns")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"users":[]}""")
      }
    }

    //#testing-bidding
    "be able to bid for campaign (POST /bidding/bid-for-campaign)" in {
      val bidRequest = BidRequest(
        id = "SGu1Jpq1IO",
        site = Site(id = "0006a522ce0f4bbbbaa6b3c38cafaa0f", domain = "fake.tld")
      )
      val bidRequestEntity = Marshal(bidRequest).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/bidding/bid-for-campaign").withEntity(bidRequestEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"description":"User Kapi created."}""")
      }
    }
  }

}
