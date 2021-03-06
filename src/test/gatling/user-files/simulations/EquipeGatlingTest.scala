import _root_.io.gatling.core.scenario.Simulation
import ch.qos.logback.classic.{Level, LoggerContext}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/**
 * Performance test for the Equipe entity.
 */
class EquipeGatlingTest extends Simulation {

    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    // Log all HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"))
    // Log failed HTTP requests
    //context.getLogger("io.gatling.http").setLevel(Level.valueOf("DEBUG"))

    val baseURL = Option(System.getProperty("baseURL")) getOrElse """http://localhost:8080"""

    val httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
        .connectionHeader("keep-alive")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0")
        .silentResources // Silence all resources like css or css so they don't clutter the results

    val headers_http = Map(
        "Accept" -> """application/json"""
    )

    val headers_http_authentication = Map(
        "Content-Type" -> """application/json""",
        "Accept" -> """application/json"""
    )

    val headers_http_authenticated = Map(
        "Accept" -> """application/json""",
        "Authorization" -> "${access_token}"
    )

    val scn = scenario("Test the Equipe entity")
        .exec(http("First unauthenticated request")
        .get("/api/account")
        .headers(headers_http)
        .check(status.is(401))
        ).exitHereIfFailed
        .pause(10)
        .exec(http("Authentication")
        .post("/api/authenticate")
        .headers(headers_http_authentication)
        .body(StringBody("""{"username":"admin", "password":"admin"}""")).asJson
        .check(header("Authorization").saveAs("access_token"))).exitHereIfFailed
        .pause(2)
        .exec(http("Authenticated request")
        .get("/api/account")
        .headers(headers_http_authenticated)
        .check(status.is(200)))
        .pause(10)
        .repeat(2) {
            exec(http("Get all equipes")
            .get("/api/equipes")
            .headers(headers_http_authenticated)
            .check(status.is(200)))
            .pause(10 seconds, 20 seconds)
            .exec(http("Create new equipe")
            .post("/api/equipes")
            .headers(headers_http_authenticated)
            .body(StringBody("""{
                "id":null
                , "titreFr":"SAMPLE_TEXT"
                , "titreEn":"SAMPLE_TEXT"
                , "titreGer":"SAMPLE_TEXT"
                , "titreSw":"SAMPLE_TEXT"
                , "souTitreFr":"SAMPLE_TEXT"
                , "souTitreEn":"SAMPLE_TEXT"
                , "souTitreGer":"SAMPLE_TEXT"
                , "souTitreSw":"SAMPLE_TEXT"
                , "nom1":"SAMPLE_TEXT"
                , "nom2":"SAMPLE_TEXT"
                , "nom3":"SAMPLE_TEXT"
                , "nom4":"SAMPLE_TEXT"
                , "nom5":"SAMPLE_TEXT"
                , "nom6":"SAMPLE_TEXT"
                , "nom7":"SAMPLE_TEXT"
                , "nom8":"SAMPLE_TEXT"
                , "prenom1":"SAMPLE_TEXT"
                , "prenom2":"SAMPLE_TEXT"
                , "prenom3":"SAMPLE_TEXT"
                , "prenom4":"SAMPLE_TEXT"
                , "prenom5":"SAMPLE_TEXT"
                , "prenom6":"SAMPLE_TEXT"
                , "prenom7":"SAMPLE_TEXT"
                , "prenom8":"SAMPLE_TEXT"
                , "qualite1":"SAMPLE_TEXT"
                , "qualite2":"SAMPLE_TEXT"
                , "qualite3":"SAMPLE_TEXT"
                , "qualite4":"SAMPLE_TEXT"
                , "qualite5":"SAMPLE_TEXT"
                , "qualite6":"SAMPLE_TEXT"
                , "qualite7":"SAMPLE_TEXT"
                , "qualite8":"SAMPLE_TEXT"
                , "image1":null
                , "image2":null
                , "image3":null
                , "image4":null
                , "image5":null
                , "image6":null
                , "image7":null
                , "image8":null
                }""")).asJson
            .check(status.is(201))
            .check(headerRegex("Location", "(.*)").saveAs("new_equipe_url"))).exitHereIfFailed
            .pause(10)
            .repeat(5) {
                exec(http("Get created equipe")
                .get("${new_equipe_url}")
                .headers(headers_http_authenticated))
                .pause(10)
            }
            .exec(http("Delete created equipe")
            .delete("${new_equipe_url}")
            .headers(headers_http_authenticated))
            .pause(10)
        }

    val users = scenario("Users").exec(scn)

    setUp(
        users.inject(rampUsers(Integer.getInteger("users", 100)) during (Integer.getInteger("ramp", 1) minutes))
    ).protocols(httpConf)
}
