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
- [x] CORS
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
- [x] Update
    - [ ] Check diff and update them
        - [x] Tags
        - [ ] externalResources
        - [x] Robots
- [x] Delete
    - [ ] Check diff and delete unrelated them
        - [x] Tags
        - [ ] externalResources
        - [x] Robots

## Tags

- [x] Create
- [x] Select
    - [x] List
        - [ ] Number of Contents by tag
    - [x] by TagName
- [x] Update
- [x] Delete
    - [x] Delete tags and related contents from junction table
    - [x] Delete junction table when delete a content

## Search

- [ ] Imple Full-text search
    - [ ] Optional Search Mode
- [x] Select
    - [x] By QueryParams
    - [ ] By Tag
    - [ ] Optional Order BY
    - [ ] Optional Limit
    - [ ] Optional Pagination

## Series

- [x] Create
- [x] Select
    - [x] Select all serieses once
    - [x] Select a series with related contents
- [x] Update
- [ ] Delete
    - [ ] Delete series and related contents from junction table
    - [ ] Delete junction table when delete a content

## Tests

- [x] Use DB

## Infrastracture

- [x] Publish Docker Image

## Others

- [x] RSS
- [x] Sitemap
- [ ] GraphQL
