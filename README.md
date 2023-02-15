# Qualtet

*Qualtet is an API-based blogging system (server-side).*

> [Live (Qualtet is a backend of this site)](https://yoshinorin.net)

|Version|Build|Coverage|Docs|
|---|---|---|---|
|![](https://img.shields.io/badge/Release-v2.3.0_(stable)-blue.svg?style=flat-square)|[![CI](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml/badge.svg)](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml)|![](https://img.shields.io/badge/coverage-upstream_issue-inactive.svg?style=flat-square)|[![](https://img.shields.io/badge/Docs-Scaladoc_&_REST_API-blue?style=flat-square)](https://yoshinorin.github.io/qualtet/docs)|
|![](https://img.shields.io/badge/Release-v1.13.0_(stale)-inactive.svg?style=flat-square)|[![CI](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml/badge.svg?branch=v1.x)](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml)|[![Coverage Status](https://coveralls.io/repos/github/yoshinorin/qualtet/badge.svg?branch=v1.x)](https://coveralls.io/github/yoshinorin/qualtet?branch=v1.x)|![](https://img.shields.io/badge/Docs-N/A-inactive.svg?style=flat-square)|


## Related Repositories

|||
|---|---|
|[Qualtet](https://github.com/yoshinorin/qualtet)|API-based blogging system (server-side).|
|[Quintet](https://github.com/yoshinorin/quintet)|The front end for Qualtet.|
|[Hexo](https://github.com/hexojs)|Hexo is SSG, but I'm using it for just local content management. (markdown, images, etc...) My website's contents depend on Hexo locally. But basically, Qualtet no needs to depend on Hexo and any SSG, cms ...etc.|

## Architecture (Example)

An example of architecture.

![](./docs/_assets/assets/arch.svg)

## Requirements

* sbt 1.8.x
* Scala 3.2.x
* Java 17.x (Perhaps works with 11.x)
* MariaDB 10.6.x
* docker & docker-compose 3.x (for test)

## Documentation

Please see [website](https://yoshinorin.github.io/qualtet/docs/).

* [Getting Started](./docs/_docs/getting-started/index.md)
    * [Docker Integration](./docs/_docs/docker/index.md)
* [Remarks](./docs/_docs/remarks/index.md)
* [REST API](https://yoshinorin.github.io/qualtet/rest-api/index.html)
    * [Examples](./docs/_docs/restapi/index.md)
* [Development](./docs/_docs/development/index.md)

## Branches

The current master branch is for `v2.x`. `v1.x` branch is [here](https://github.com/yoshinorin/qualtet/tree/v1.x). But it will be not updated.

## License

This code is open source software licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
