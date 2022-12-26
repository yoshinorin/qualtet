---
layout: doc-page
title: REST API
---

## Open API Docs

Please see [other docs](https://yoshinorin.github.io/qualtet/rest-api/index.html).

## Examples

REST API execution examples.

### Generate ID Token

```sh
$ curl -D - -X POST -H "Content-Type: application/json" -d '{"authorId":"01fgvhkzpyghp23wvp4p87nx29","password":"pass"}' 127.0.0.1:9001/token/
HTTP/1.1 201 Created
Server: akka-http/10.2.4
Date: Sun, 08 Aug 2021 16:39:21 GMT
Content-Type: application/json
Content-Length: 638

{
  "token" : "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3Mi...."
}
```

### POST a content

TODO
