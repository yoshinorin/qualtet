---
layout: doc-page
title: Features
---

## Auth

- [x] JWT
  - [x] Configurable expiration

## Protection

- [ ] UA Filter
- [ ] IP Filter
- [ ] CORS
- [ ] DDOS

## Caching

- [x] Cache Flash API
- [x] Sitemap Caching
    - [x] Configurable
- [x] ContentTypes Caching
    - [x] Configurable
- Maybe I need others...

## Contents

- [x] Create
- [x] Select
    - [ ] Return id
    - [ ] GET API migrate from `path` to `id`
- [x] Update
    - [ ] Check diff and delete unrelated them or update them
        - [x] Tags
        - [ ] externalResources
        - [x] Robots
- [x] Delete
    - [x] Delete with externalResources, Tags & Robots

## Tags

- [x] Create
- [x] Select
    - [x] List
        - [ ] Number of Contents by tag
    - [x] by TagName
- [x] Update
- [x] Delete

## Search

- [x] Imple Full-text search
- [x] Select
    - [x] By QueryParams
    - [ ] By Tag
    - [ ] Order by option
    - [ ] Limit option
    - [ ] Pagination option

## Tests

- [x] Use DB

## Infrastracture

- [x] Publish Docker Image

## Others

- [x] RSS
- [x] Sitemap
- [ ] GraphQL
