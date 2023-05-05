---
layout: doc-page
title: Development
---

## Run local server

### Start server

```sh
$ cd <source code dir>
$ sbt run
Multiple main classes detected. Select one to run:
 [1] net.yoshinorin.qualtet.BootStrap
 [2] net.yoshinorin.qualtet.tasks.CreateAuthor

Enter number: 1
```

### Start server(Hot reload)

```
$ sbt
$ ~reStart
```

Run `scalafmt` & `kill current server` before start server(Hot reload)

```
$ sbt
$ runs
```

### Kill current server process

```
$ sbt
$ kills
```

### local db using by docker

```
$ sbt

// start up local db using by docker
$ localDbUp

// shutdown local db
$ localDbUp
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

## Check Dependency updates

> [refs: sbt-updates](https://github.com/rtimush/sbt-updates)

```
sbt:qualtet> dependencyUpdates
[info] Found 8 dependency updates for qualtet
...
[success] Total time: 3 s, completed 2022/12/27 12:00:26
sbt:qualtet>
```

## Generate commit log for release note

```sh
// git log --pretty=format:"* (%h) %s" <tag>..<tag>
$ git log --pretty=format:"* (%h) %s" v1.2.0..v1.3.0
```

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

## Assembly

```
$ sbt assembly
```

## Documentation

### REST API Document

```sh
$ cd ./docs/api
$ npm run serve
Server started: http://127.0.0.1:8080
```

### Generate Website docs

```sh
$ sbt doc
```

Serve generated docs locally.

```sh
$ cd ./docs/dist
$ python -m http.server 8080
```
