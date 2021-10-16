# [WIP] Qualtet

|Build|Coverage|API Doc|
|---|---|---|
|[![CI](https://img.shields.io/github/workflow/status/yoshinorin/qualtet/CI/master?label=CI)](https://github.com/yoshinorin/qualtet/actions)|[![Coverage Status](https://coveralls.io/repos/github/yoshinorin/qualtet/badge.svg?branch=master)](https://coveralls.io/github/yoshinorin/qualtet?branch=master)|[![](https://img.shields.io/badge/Doc-Swagger-blue.svg)](https://yoshinorin.github.io/qualtet/)|

## Related Repositories

|||
|---|---|
|[Hexo](https://github.com/hexojs/hexo)|CLI for my blog.|
|[Conmas](https://github.com/yoshinorin/conmas)|Assistant CLI tool for POST the Hexo content to Qualtet.|
|[Qualtet](https://github.com/yoshinorin/qualtet)|Server side of blogging system.|
|[Quintet](https://github.com/yoshinorin/quintet)|The front end for Qualtet.|

## Documentation

* [API](https://yoshinorin.github.io/qualtet/)
## Requirements

* sbt 1.5.x
* Scala 2.13.x
* JVM 11.x
* MariaDB 10.5.x

## Set up

Create a database sachema before install application. Also, schema name is anything will be fine.

```sql
CREATE DATABASE qualtet;
```

### Create an author

Qualtet does not support signup endpoint. You have to create an author with `sbt task`, like below.

```scala
$ sbt createAuthor <name> <displayName> <password>

// example
$ sbt createAuthor jhonDue JD pass

// result
2021-08-03 21:54:03 +0900 [INFO] from net.yoshinorin.qualtet.tasks.createAuthor$ - user created: {
  "id" : "086f0af1-6843-4116-96ee-73011b8538bb",
  "name" : "jhonDue",
  "displayName" : "JD",
  "createdAt" : 1627995242
}
```

## Examples

API execution examples

### Generate ID Token

```
$ curl -D - -X POST -H "Content-Type: application/json" -d '{"authorId":"dbed0c8e-57b9-4224-af10-c2ee9b49c066","password":"pass"}' 127.0.0.1:9001/token/
HTTP/1.1 201 Created
Server: akka-http/10.2.4
Date: Sun, 08 Aug 2021 16:39:21 GMT
Content-Type: application/json
Content-Length: 638

{
  "token" : "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3Mi...."
}
```

## Development

### Compile

```sh
$ cd <source code dir>
$ sbt
...
[info] started sbt server
sbt:qualtet> ~compile
[success] Total time: 1 s, completed 2021/05/12 2:49:37
[info] 1. Monitoring source files for qualtet/compile...
[info]    Press <enter> to interrupt or '?' for more options.
```

### Start server

```sh
$ cd <source code dir>
$ sbt run
[success] Total time: 5 s, completed 2021/05/12 3:01:27
```

### Start server(Hot reload)

```
$ sbt
$ ~reStart
```

### API Document

```sh
$ cd ./docs/api
$ npm run serve
Server started: http://127.0.0.1:8080
```

### Code format

```sh
$ scalafmt
```

### Test

Run all tests

```sh
$ sbt test
```

Run specific test


```sh
$ sbt
$ testOnly *xxxxxSpec
```

Generate Coverage report

```sh
$ sbt coverageReport

or

$ sbt clean coverage test coverageReport
```
