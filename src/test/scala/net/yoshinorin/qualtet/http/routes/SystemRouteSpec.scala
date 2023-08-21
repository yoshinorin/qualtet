package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.fixture.Fixture

import cats.effect.unsafe.implicits.global
import net.yoshinorin.qualtet.config.HttpSystemEndpointConfig
import net.yoshinorin.qualtet.config.HttpSystemEndpointMetadata

// testOnly net.yoshinorin.qualtet.http.routes.SystemRouteSpec
class SystemRouteSpec extends AnyWordSpec {

  val systemRoute: SystemRoute = new SystemRoute(HttpSystemEndpointConfig(metadata = HttpSystemEndpointMetadata(enabled = false)))
  val router = Fixture.makeRouter(systemRoute = systemRoute)
  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  val enabledMetadataEndpointSystemRoute: SystemRoute = new SystemRoute(HttpSystemEndpointConfig(metadata = HttpSystemEndpointMetadata(enabled = true)))
  val enabledMetadataEndpointRouter = Fixture.makeRouter(systemRoute = enabledMetadataEndpointSystemRoute)
  val clientForEnabledMetadataEndpoint: Client[IO] = Client.fromHttpApp(enabledMetadataEndpointRouter.routes.orNotFound)

  "SystemRoute" should {

    "be return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/system"))
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
          }
        }
        .unsafeRunSync()
    }

    "health" should {
      "be return 200" in {
        client
          .run(Request(method = Method.GET, uri = uri"/system/health"))
          .use { response =>
            IO {
              assert(response.status === Ok)
            }
          }
          .unsafeRunSync()
      }
    }

    "metadata" should {
      "be return 200 if config.http.endpoints.system.metadata is enabled" in {
        clientForEnabledMetadataEndpoint
          .run(Request(method = Method.GET, uri = uri"/system/metadata"))
          .use { response =>
            IO {
              assert(response.status === Ok)
              assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("name"))
              assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("version"))
              assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("scalaVersion"))
              assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("sbtVersion"))
              assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("commitHash"))
              assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("runtime"))
              assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("jvmVendor"))
              assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("runtimeVersion"))
            }
          }
          .unsafeRunSync()
      }

      "be return 404 if config.http.endpoints.system.metadata is disabled" in {
        client
          .run(Request(method = Method.GET, uri = uri"/system/metadata"))
          .use { response =>
            IO {
              assert(response.status === NotFound)
            }
          }
          .unsafeRunSync()
      }
    }

  }

}
