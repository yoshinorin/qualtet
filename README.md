# [WIP] Qualtet

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
