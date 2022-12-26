---
layout: doc-page
title: Getting Started
---

## Create database

Create a database schema before install an application. Also, schema name is anything will be fine.

```sql
CREATE DATABASE qualtet;
```

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
