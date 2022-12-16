# Features

## TypeSafe

- [x] Value object
- [ ] Endpoint typesafe: https://github.com/softwaremill/tapir
- [ ] Refined: https://github.com/fthomas/refined

## Auth

- [x] Add Password column
- [x] Key pair
- [ ] Logging
- [x] Generate JWT
    - [x] Configurable expiration
- [x] Validate JWT

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

- [ ] Imple Full-text search
- [ ] Select
    - [ ] By QueryParams

## Tests

- [x] Use DB

## Infrastracture

- [x] Publish Docker Image

## Others

- [x] RSS
- [x] Sitemap
- [ ] GraphQL
