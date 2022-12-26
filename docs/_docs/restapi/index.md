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
$ curl -D - -X POST -H "Content-Type: application/json" -d '{"authorId":"01gn798rem0pj4xnzxfnj5g5wp","password":"pass"}' 127.0.0.1:9001/token/
HTTP/1.1 201 Created
Date: Sun, 08 Aug 2021 16:39:21 GMT
Content-Type: application/json
Content-Length: 638

{
  "token" : "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3Mi...."
}
```

### POST a content (TODO)

```sh
$ curl -D - -X POST -H "Authorization: Bearer <token>" -d '{"TODO"}' 127.0.0.1:9001/contents
```

## DELETE a content

```sh
$ curl -D - -X DELETE -H "Authorization: Bearer <token>" 127.0.0.1:9001/contents/<contentsId>
HTTP/1.1 204 No Content
Date: Mon, 26 Dec 2022 13:40:44 GMT
Connection: keep-alive
```
