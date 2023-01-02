---
layout: doc-page
title: Getting Started
---

## Create database

Create a database schema before install an application. Also, schema name is anything will be fine.

```sql
CREATE DATABASE qualtet;
```

## Configuration

Qualtet reads all settings from the system environment variable. You have to set the following system environment variables.

### Database

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_DB_DATASOURCE_URL`|Data source url for JDBC connection.|`string`|-|`jdbc:mariadb://127.0.0.1/cahsper?useUnicode=true&characterEncoding=utf8mb4`|
|`QUALTET_DB_USER`|Database user name.|`string`|-|`root`|
|`QUALTET_DB_PASSWORD`|Database user password.|`string`|-|`pass`|

### HTTP Server

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_HTTP_BIND_ADDRESS`|Http server bind address.|`string`|`127.0.0.1`|`0.0.0.0`|
|`QUALTET_HTTP_PORT`|Http server port.|`int`|`9001`|`9001`|

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

## Create an author

Qualtet does not support the signup endpoint. Need to create an author with `sbt task`, like below.

```scala
$ sbt

...

$ sbt:qualtet> createAuthor <name> <displayName> <password>

// example
$ sbt:qualtet> createAuthor jhonDue JD pass

// result
2021-08-03 21:54:03 +0900 [INFO] from net.yoshinorin.qualtet.tasks.createAuthor$ - author created: {
  "id" : "01fgvhkzpyghp23wvp4p87nx29",
  "name" : "jhonDue",
  "displayName" : "JD",
  "createdAt" : 1627995242
}
```
