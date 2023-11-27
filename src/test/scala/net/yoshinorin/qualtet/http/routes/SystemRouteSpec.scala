package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.ApplicationInfo
import net.yoshinorin.qualtet.buildinfo.BuildInfo
import net.yoshinorin.qualtet.config.{HttpSystemEndpointConfig, HttpSystemEndpointMetadata}
import net.yoshinorin.qualtet.fixture.Fixture.{makeRouter, unsafeDecode}

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.SystemRouteSpec
class SystemRouteSpec extends AnyWordSpec {

  val systemRoute: SystemRoute = new SystemRoute(HttpSystemEndpointConfig(metadata = HttpSystemEndpointMetadata(enabled = false)))
  val router = makeRouter(systemRoute = systemRoute)
  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  val enabledMetadataEndpointSystemRoute: SystemRoute = new SystemRoute(HttpSystemEndpointConfig(metadata = HttpSystemEndpointMetadata(enabled = true)))
  val enabledMetadataEndpointRouter = makeRouter(systemRoute = enabledMetadataEndpointSystemRoute)
  val clientForEnabledMetadataEndpoint: Client[IO] = Client.fromHttpApp(enabledMetadataEndpointRouter.routes.orNotFound)

  "SystemRoute" should {

    "be return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/system"))
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
            assert(response.contentType.isEmpty)
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
              assert(response.contentType.isEmpty)
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
              assert(response.contentType.get === `Content-Type`(MediaType.application.json))

              val maybeAppInfo = unsafeDecode[ApplicationInfo](response)
              assert(maybeAppInfo.name === BuildInfo.name)
              assert(maybeAppInfo.repository === BuildInfo.repository)
              assert(maybeAppInfo.version === BuildInfo.version)
              assert(maybeAppInfo.build.scalaVersion === BuildInfo.scalaVersion)
              assert(maybeAppInfo.build.sbtVersion === BuildInfo.sbtVersion)
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
              // TODO: consider to return JSON or not
              assert(response.contentType.isEmpty)
            }
          }
          .unsafeRunSync()
      }
    }

  }

}
