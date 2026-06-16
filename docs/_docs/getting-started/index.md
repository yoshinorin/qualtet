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

### KeyPair (JWT signing)

Qualtet signs / verifies JWT with an asymmetric keypair. The keypair source is configurable.

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_KEYPAIR_SOURCE`|Source of the keypair: `IN-MEMORY`, `FILE`, or `PEM`|`string`|`IN-MEMORY`|`FILE`|
|`QUALTET_KEYPAIR_ALGORITHM`|Key algorithm|`string`|`RSA`|`RSA`|
|`QUALTET_KEYPAIR_LENGTH`|Key length in bits (`IN-MEMORY` only)|`int`|`2048`|`2048`|
|`QUALTET_KEYPAIR_SECURE_RANDOM`|`SecureRandom` algorithm name (`IN-MEMORY` only). Uses `SecureRandom.getInstanceStrong` when unset.|`string`|-|`NativePRNGNonBlocking`|
|`QUALTET_KEYPAIR_PUBLIC_KEY_PATH`|Path to public key PEM (`FILE` only). X.509 / SubjectPublicKeyInfo (`-----BEGIN PUBLIC KEY-----`)|`string`|-|`/etc/qualtet/keys/public.pem`|
|`QUALTET_KEYPAIR_PRIVATE_KEY_PATH`|Path to private key PEM (`FILE` only). PKCS#8 (`-----BEGIN PRIVATE KEY-----`)|`string`|-|`/etc/qualtet/keys/private.pem`|
|`QUALTET_KEYPAIR_PUBLIC_KEY_PEM`|Public key PEM as a string (`PEM` only)|`string`|-|`-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----`|
|`QUALTET_KEYPAIR_PRIVATE_KEY_PEM`|Private key PEM as a string (`PEM` only)|`string`|-|`-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----`|

#### `IN-MEMORY` (default)

The keypair is generated on boot with `KeyPairGenerator` and lost on restart. Existing JWTs become invalid after restart. Convenient for development.

#### `FILE`

The keypair is loaded from PEM files at boot. The public key must be X.509 / SubjectPublicKeyInfo and the private key must be PKCS#8.

```sh
# Generate a keypair (one-off)
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl pkey -in private.pem -pubout -out public.pem

# Configure
env QUALTET_KEYPAIR_SOURCE=FILE \
    QUALTET_KEYPAIR_PUBLIC_KEY_PATH=/etc/qualtet/keys/public.pem \
    QUALTET_KEYPAIR_PRIVATE_KEY_PATH=/etc/qualtet/keys/private.pem
```

If you have a PKCS#1 (`-----BEGIN RSA PRIVATE KEY-----`) key, convert it to PKCS#8 first:

```sh
openssl pkcs8 -topk8 -nocrypt -in pkcs1.pem -out pkcs8.pem
```

#### `PEM`

The keypair is passed as PEM strings (typically injected via environment variable). Useful for environments that don't have a persistent filesystem (e.g. some container platforms).

```sh
env QUALTET_KEYPAIR_SOURCE=PEM \
    QUALTET_KEYPAIR_PUBLIC_KEY_PEM="$(cat public.pem)" \
    QUALTET_KEYPAIR_PRIVATE_KEY_PEM="$(cat private.pem)"
```

### Cacheing

Some of endpoint has in-memory cache.

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_CACHE_CONTENT_TYPE `|content type cache (sec)|`int`|`604800`|-|
|`QUALTET_CACHE_SITEMAP `|sitemap cache (sec)|`int`|`3600`|-|
|`QUALTET_CACHE_FEED `|feed cache (sec)|`int`|`7200`|-|
|`QUALTET_CACHE_TAGS `|tags cache (sec)|`int`|`7200`|-|

### Feed

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_FEED_LIMIT `|Number of articles to include in the feed|`int`|`5`|-|

### Search

Qualtet using `full-text-search`. So, please specify `--innodb-ft-min-token-size=<n>` on your MariaDB.

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_SEARCH_MAX_NUM_OF_WORDS `|Maximum specifiable search words|`int`|`3`|-|
|`QUALTET_SEARCH_WORD_MIN_LENGTH `|Minimum length of search word|`int`|`2`|-|
|`QUALTET_SEARCH_WORD_MAX_LENGTH `|Maximum length of search word|`int`|`15`|-|

### OpenTelemetry (Optional)

Qualtet supports OpenTelemetry for distributed tracing and observability. OpenTelemetry is completely optional and disabled by default.

|Property|Description|Type|Default|Example|
|---|---|---|---|---|
|`QUALTET_OTEL_ENABLED`|Enable OpenTelemetry tracing|`boolean`|`false`|`true`|
|`QUALTET_OTEL_SERVICE_NAME`|Service name for OpenTelemetry|`string`|`qualtet`|`my-service`|
|`QUALTET_OTEL_SERVICE_NAMESPACE`|Service namespace for OpenTelemetry|`string`|-|`production`|
|`QUALTET_OTEL_EXPORTER_ENDPOINT`|OTLP exporter endpoint URL|`string`|-|`http://localhost:4317`|
|`QUALTET_OTEL_EXPORTER_PROTOCOL`|OTLP protocol (`grpc` or `http/protobuf`)|`string`|`http/protobuf`|`grpc`|
|`QUALTET_OTEL_EXPORTER_HEADERS`|OTLP exporter headers for authentication|`string`|-|`Authorization=Bearer token123`|
|`QUALTET_OTEL_PROPAGATOR`|Which propagator to use|`string`|`tracecontext`|`tracecontext,baggage`|

These settings allow you to configure OpenTelemetry tracing for monitoring HTTP requests, database operations, and other application metrics. The traces can be exported to OTLP-compatible systems.

**Protocol:** Qualtet supports both gRPC and HTTP protocols:
- `grpc`: Use for direct communication with OpenTelemetry Collector (typically port 4317)
- `http/protobuf`: Use for HTTP-based OTLP endpoints like Grafana Cloud (typically port 4318)

**Authentication:** If needed, use `QUALTET_OTEL_EXPORTER_HEADERS` to provide authentication credentials. Multiple headers can be specified using comma separation: `key1=value1,key2=value2`.

**Fiber Context Tracking:** For better trace correlation with OpenTelemetry, enable Cats Effect fiber context tracking by adding `-Dcats.effect.trackFiberContext=true` to `JAVA_OPTS`:

When running with Docker:

```yaml
environment:
  JAVA_OPTS: "-Xms512M -Xmx768M -Dcats.effect.trackFiberContext=true"
```

When running jar directly:

```sh
java -Dcats.effect.trackFiberContext=true -jar qualtet-assembly.jar
```

This option provides more detailed tracing information and better trace correlation across asynchronous operations.

**Note:** The service version is automatically set from the application build version.

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
