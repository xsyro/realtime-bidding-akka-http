## **REAL-TIME BIDDING**

A real-time bidding agent using Scala and Akka ToolKits

### ****Technical Requirements****
* Minimum of Java 8 Version
* SbtVersion=1.4.6
* ScalaVersion=2.12.7
* AkkaVersion=2.6.14
* AkkaHttpVersion=10.2.4
* Using Scala, Akka Actors for the bidding agent implementation
* Using Akka HTTP for HTTP server and receiving and responding requests

### **Get Started(Main) Class**
* Run _com.rtb.StarHttpServer.scala_ - Application is default to run port 9001. This can be changed in application.conf file
* Run Test Spec in _com.rtb.BiddingRoutesSpec_. The test cases can be used to ensure the application runs as expected.