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

### DELETE a content

```sh
$ curl -D - -X DELETE -H "Authorization: Bearer <token>" 127.0.0.1:9001/contents/<contentsId>
HTTP/1.1 204 No Content
Date: Mon, 26 Dec 2022 13:40:44 GMT
Connection: keep-alive
```

### Search

The search endpoint validates query parameters. Not all, but some examples are below.

```sh
# OK
$ curl -D - -X GET 127.0.0.1:9231/search?q=test&q=hoge&q=fuga
HTTP/1.1 200 OK
Date: Mon, 02 Jan 2023 13:14:43 GMT
Connection: keep-alive
Content-Type: application/json
Content-Length: 7815

{"count":19,"contents":[{"path":"....}]}

# NG1
$ curl -D - -X GET 127.0.0.1:9231/search?q=tes
HTTP/1.1 400 Bad Request
Date: Mon, 02 Jan 2023 13:16:50 GMT
Connection: keep-alive
Content-Type: application/json
Content-Length: 42

{"message":"SEARCH_CHAR_LENGTH_TOO_SHORT"}

# NG2
$ curl -D - -X GET 127.0.0.1:9231/search
HTTP/1.1 400 Bad Request
Date: Mon, 02 Jan 2023 13:17:32 GMT
Connection: keep-alive
Content-Type: application/json
Content-Length: 35

{"message":"SEARCH_QUERY_REQUIRED"}

# NG3
$ curl -D - -X GET 127.0.0.1:9231/search?q=a.b.c
HTTP/1.1 400 Bad Request
Date: Mon, 02 Jan 2023 13:19:23 GMT
Connection: keep-alive
Content-Type: application/json
Content-Length: 36

{"message":"INVALID_CHARS_INCLUDED"}
```
