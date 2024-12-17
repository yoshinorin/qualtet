---
layout: doc-page
title: Getting Started
---

## Create database

Create a database schema before installing the application. Any schema name will be fine.

```sql
CREATE DATABASE qualtet;
```

## Configuration

Qualtet reads all settings from system environment variables. You have to set the following system environment variables.

### Database

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_DB_DATASOURCE_URL`|Data source URL for JDBC connection.|`string`|-|`jdbc:mariadb://127.0.0.1/cahsper?useUnicode=true&characterEncoding=utf8mb4`|
|`QUALTET_DB_USER`|Database user name.|`string`|-|`root`|
|`QUALTET_DB_PASSWORD`|Database user password.|`string`|-|`pass`|
|`QUALTET_DB_CONNECTION_POOL_MAX_LIFETIME`|Maximum lifetime of a connection in the pool.|`int`|`1800000`|`1800000`|
|`QUALTET_DB_CONNECTION_POOL_MAX_POOLSIZE`|Maximum size of the pool.|`int`|`10`|`10`|

Please see the [HikariCP doc](https://github.com/brettwooldridge/HikariCP) for details of the connection pool settings.

### HTTP Server

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_HTTP_BIND_ADDRESS`|HTTP server bind address.|`string`|`127.0.0.1`|`0.0.0.0`|
|`QUALTET_HTTP_PORT`|HTTP server port.|`int`|`9001`|`9001`|

### Endpoints

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_HTTP_ENDPOINT_SYSTEM_METADATA_ENABLED`| Allow to return application metadata (e.g Java version, commit hash etc). |`boolean`|`false`|`false`|

### CORS

By default Qualtet allow CORS requests from any origin.

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_CORS_ALLOW_ORIGINS.<n>`|CORS allow origin|`string`| - |`http://localhost:8080`|

Qualtet can specify multiple allow-origins.

```sh
# example
env 'QUALTET_CORS_ALLOW_ORIGINS.0'="http://localhost:8080"
env 'QUALTET_CORS_ALLOW_ORIGINS.1'="http://127.0.0.1:8080"
env 'QUALTET_CORS_ALLOW_ORIGINS.2'="http://localhost:3000"
...
```

### Authentication

Qualtet uses JWT for auth endpoint. You have to set `ISS` and `AUD` for generationg JWT.

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_JWT_ISS `|jwt iss|`string`|-|-|
|`QUALTET_JWT_AUD `|jwt aud|`string`|-|-|
|`QUALTET_JWT_EXPIRATION `|jwt expiration time (sec)|`int`|`3600`|-|

### Cacheing

Some of endpoint has in-memory cache.

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_CACHE_CONTENT_TYPE `|content type cache (sec)|`int`|`604800`|-|
|`QUALTET_CACHE_SITEMAP `|sitemap cache (sec)|`int`|`3600`|-|
|`QUALTET_CACHE_FEED `|feed cache (sec)|`int`|`7200`|-|

### Search

Qualtet using `full-text-search`. So, please specify `--innodb-ft-min-token-size=<n>` on your MariaDB.

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_SEARCH_MAX_NUM_OF_WORDS `|Maximum specifiable search words|`int`|`3`|-|
|`QUALTET_SEARCH_WORD_MIN_LENGTH `|Minimum length of search word|`int`|`2`|-|
|`QUALTET_SEARCH_WORD_MAX_LENGTH `|Maximum length of search word|`int`|`15`|-|

## Create an author

Qualtet does not support the signup endpoint. Need to create an author with `sbt task`, like below.

### with sbt

```scala
$ sbt

...

$ sbt:qualtet> createOrUpdateAuthor <name> <displayName> <password>

// example
$ sbt:qualtet> createOrUpdateAuthor jhonDue JD pass

// result
2021-08-03 21:54:03 +0900 [INFO] from net.yoshinorin.qualtet.tasks.CreateOrUpdateAuthor$ - author created: {
  "id" : "01fgvhkzpyghp23wvp4p87nx29",
  "name" : "jhonDue",
  "displayName" : "JD",
  "createdAt" : 1627995242
}
```

### with java

```sh
$ java -cp qualtet-assembly.jar net.yoshinorin.qualtet.tasks.CreateOrUpdateAuthor <name> <displayName> <password>
```
