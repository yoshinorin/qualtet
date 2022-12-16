# Qualtet

*Qualtet is an API-based blogging system (server-side).*

> [Live (Qualtet is a backend of this site)](https://yoshinorin.net)

|Version|Build|Coverage|API Doc|
|---|---|---|---|
|![](https://img.shields.io/badge/Release-v2.0.0_(WIP)-blue.svg?style=flat-square)|[![CI](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml/badge.svg)](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml)|[![Coverage Status](https://coveralls.io/repos/github/yoshinorin/qualtet/badge.svg?branch=master)](https://coveralls.io/github/yoshinorin/qualtet?branch=master)|[![](https://img.shields.io/badge/Doc-Swagger-blue.svg)](https://yoshinorin.github.io/qualtet/)|
|![](https://img.shields.io/badge/Release-v1.13.0_(stable)-blue.svg?style=flat-square)|[![CI](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml/badge.svg?branch=v1.x)](https://github.com/yoshinorin/qualtet/actions/workflows/ci.yml)|[![Coverage Status](https://coveralls.io/repos/github/yoshinorin/qualtet/badge.svg?branch=v1.x)](https://coveralls.io/github/yoshinorin/qualtet?branch=v1.x)|[![](https://img.shields.io/badge/Doc-Swagger-blue.svg)](https://yoshinorin.github.io/qualtet/)|


## Related Repositories

|||
|---|---|
|[Qualtet](https://github.com/yoshinorin/qualtet)|API-based blogging system (server-side).|
|[Quintet](https://github.com/yoshinorin/quintet)|The front end for Qualtet.|
|[Hexo](https://github.com/hexojs)|Hexo is SSG, but I'm using it for just local content management. (markdown, images, etc...) My website's contents depend on Hexo locally. But basically, Qualtet no needs to depend on Hexo and any SSG, cms ...etc.|

## Architecture (Example)

An example of architecture.

![](./docs/arch.svg)

## Requirements

* sbt 1.7.x
* Scala 2.13.x
* Java 11.x, 17.x
* MariaDB 10.6.x
* docker & docker-compose 3.x (for test)

## Documentation

* [API](https://yoshinorin.github.io/qualtet/)
* [Features](./docs/features.md)
* [Set up](./docs/setup.md)
* [Remarks](./docs/remarks.md)
* [Examples](./docs/examples.md)
* [ER Diagram](./docs/erd.md)
* [Development](./docs/development.md)

## Branches

The current master branch is for `v2.x`. `v1.x` branch is [here](https://github.com/yoshinorin/qualtet/tree/v1.x). But it will be not updated.

## License

This code is open source software licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
