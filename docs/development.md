# Development

## Compile

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

## Start server

```sh
$ cd <source code dir>
$ sbt run
Multiple main classes detected. Select one to run:
 [1] net.yoshinorin.qualtet.BootStrap
 [2] net.yoshinorin.qualtet.tasks.CreateAuthor

Enter number: 1
```

## Start server(Hot reload)

```
$ sbt
$ ~reStart
```

## Assembly

```
$ sbt assembly
```

## API Document

```sh
$ cd ./docs/api
$ npm run serve
Server started: http://127.0.0.1:8080
```

## Code format

```sh
$ scalafmt
```

## Test

Run all tests with db (docker container)

```
$ sbt testWithDb
```

Run specific test

```sh
# NOTE: Many of test depends on DB. This command may not works well...
$ sbt
$ testOnly *xxxxxSpec
```

Generate Coverage report

```sh
$ sbt coverageReport

or

$ sbt clean coverage testWithDb coverageReport
```

## Change log level in sbt

```scala
$ sbt
...

> debug
```

> [Change the logging level for a specific task, configuration, or project](https://www.scala-sbt.org/1.x/docs/Howto-Logging.html#Change+the+logging+level+for+a+specific+task%2C+configuration%2C+or+project)

## Check Scala 3

```scala
$ sbt migrate-libs root

// https://scalacenter.github.io/scala-3-migration-guide/docs/tooling/scala-3-migrate-plugin.html
```

## Generate commit log for release note

```sh
// git log --pretty=format:"* (%h) %s" <tag>..<tag>
$ git log --pretty=format:"* (%h) %s" v1.2.0..v1.3.0
```