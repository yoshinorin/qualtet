# Qualtet

*Qualtet is an API-based blogging system (server-side).*

> [Live (Qualtet is the backend for this site)](https://yoshinorin.net)

> [Docs](https://yoshinorin.github.io/qualtet/docs) | [Scala API](https://yoshinorin.github.io/qualtet/) | [REST API](https://yoshinorin.github.io/qualtet/rest-api)

|| Version | CI/CD | Coverage |
|---|---|---|---|
| `v3.x` |![](https://img.shields.io/github/v/release/yoshinorin/qualtet?sort=semver&style=flat&label=Release)|[![CI](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml/badge.svg)](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml)|[![Coverage Status](https://coveralls.io/repos/github/yoshinorin/qualtet/badge.svg?branch=master)](https://coveralls.io/github/yoshinorin/qualtet?branch=master)|
| `v2.x` |![](https://img.shields.io/badge/Release-v2.21.0-blue.svg?style=flat-square)|[![CI](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml/badge.svg)](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml)|[![Coverage Status](https://coveralls.io/repos/github/yoshinorin/qualtet/badge.svg?branch=v2.x)](https://coveralls.io/github/yoshinorin/qualtet?branch=v2.x)|
| `v1.x` |![](https://img.shields.io/badge/Release-v1.13.0_(stale)-inactive.svg?style=flat-square)|[![CI](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml/badge.svg?branch=v1.x)](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml)|[![Coverage Status](https://coveralls.io/repos/github/yoshinorin/qualtet/badge.svg?branch=v1.x)](https://coveralls.io/github/yoshinorin/qualtet?branch=v1.x)|

## Related Projects

| Project | Description |
|---|---|
|[Qualtet](https://github.com/yoshinorin/qualtet)|API-based blogging system (server-side).|
|[Qualtet-mock](https://github.com/yoshinorin/qualtet-mock)| Mock server of [Qualtet](https://github.com/yoshinorin/qualtet). Created for [Quintet](https://github.com/yoshinorin/quintet)'s E2E tests.|
|[Qualtet-CLI](https://github.com/yoshinorin/qualtet-cli)|A set of wrappers that call the APIs of [Hexo](https://github.com/hexojs/hexo) and [Qualtet](https://github.com/yoshinorin/qualtet).|
|[Quintet](https://github.com/yoshinorin/quintet)|The front end for Qualtet.|

<sub>About [Hexo](https://github.com/hexojs). Hexo is an SSG, but I'm using it only for local content management (such as markdown, images, etc.). The content of my website depends on Hexo locally. However, Qualtet doesn't necessarily need to depend on Hexo or any other SSG or CMS, etc.</sub>

## Architecture (Example)

An example architecture.

![](./docs/_assets/assets/arch.svg)

## Requirements

* sbt 1.12.x
* Scala 3.8.x
* Java 25.x, 21.x (Perhaps works with 17.x)
* MariaDB 11.8.x (Perhaps works with 11.4.x)
* docker & docker-compose 3.x (for test)

## Documentation

Please see [website](https://yoshinorin.github.io/qualtet/docs/).

* [Getting Started](./docs/_docs/getting-started/index.md)
    * [Docker Integration](./docs/_docs/docker/index.md)
* [Remarks](./docs/_docs/remarks/index.md)
* [REST API](https://yoshinorin.github.io/qualtet/rest-api/index.html)
    * [Examples](./docs/_docs/restapi/index.md)
* [Development](./docs/_docs/development/index.md)

## Releases

Please see [releases page](./docs/_docs/releases/index.md).

## ERD

![](./docs/_assets/assets/erd.png)

## Using Stacks

| | Technology | Purpose |
|----------|------------|---------|
| **Core** | [Scala](https://www.scala-lang.org/) | Primary programming language |
| | [cats](https://github.com/typelevel/cats) | Functional programming library |
| | [cats-effect](https://github.com/typelevel/cats-effect) | Pure asynchronous runtime |
| **HTTP/API** | [http4s](https://github.com/http4s/http4s) | HTTP server |
| | [jsoniter-scala](https://github.com/plokhotnyuk/jsoniter-scala) | JSON codec |
| | [jwt-scala](https://github.com/jwt-scala/jwt-scala) | JWT authentication support |
| **Database** | [doobie](https://github.com/tpolecat/doobie) | Functional JDBC layer |
| | [MariaDB JDBC Driver](https://mariadb.com/kb/en/mariadb-connector-j/) | Database connectivity |
| | [Flyway](https://flywaydb.org/) | Database migration management |
| **Observability** | [OpenTelemetry](https://opentelemetry.io/) | Distributed tracing and metrics |
| | [otel4s](https://github.com/typelevel/otel4s) | Scala OpenTelemetry integration |
| **Logging** | [logback](https://github.com/qos-ch/logback) | Logging implementation |
| | [slf4j](https://github.com/qos-ch/slf4j) | Logging facade |
| | [Logstash Encoder](https://github.com/logfellow/logstash-logback-encoder) | Structured logging |
| **Configuration** | [Typesafe Config](https://github.com/lightbend/config) | Configuration management |
| **Utils** | [airframe-ulid](https://github.com/wvlet/airframe/) | ULID generation |
| | [caffeine](https://github.com/ben-manes/caffeine) | Caching |
| | [Spring Security](https://github.com/spring-projects/spring-security) | Password hashing |
| **Testing** | [ScalaTest](http://www.scalatest.org/) | Testing framework |
| | [Mockito](https://github.com/mockito/mockito) | Mock |
| **DevOps** | [Scalafmt](https://scalameta.org/scalafmt/) | Code formatter |
| | [GitHub Actions](https://github.com/yoshinorin/qualtet/actions) | CI/CD pipeline |
| | [Coveralls](https://coveralls.io/github/yoshinorin/qualtet) | Code coverage reporting |
| | [ReDoc](https://github.com/Redocly/redoc) | API documentation generation |
| | [GitHub Pages](https://pages.github.com/) | Documentation hosting |

## Branches

The current master branch is for `v2.x`. `v1.x` branch is [here](https://github.com/yoshinorin/qualtet/tree/v1.x). But it will be not updated.

## License

This code is open source software licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
