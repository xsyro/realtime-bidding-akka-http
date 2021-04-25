lazy val akkaHttpVersion = "10.2.4"
lazy val akkaVersion    = "2.6.14"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "dev.jamiu",
      scalaVersion    := "2.12.7",
      javaOptions += s"-Dconfig.resource=application.conf",
    )),
    name := "realtime-bidding-akka-http",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",

      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.1.4"         % Test
    )
  )
