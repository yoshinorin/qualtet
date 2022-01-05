# Features & implementation todo

## TypeSafe

- [x] Value object
- [ ] Endpoint typesafe: https://github.com/softwaremill/tapir
- [ ] Refined: https://github.com/fthomas/refined

## Auth

- [x] Add Password column
- [x] Key pair
- [ ] Logging
- [x] Generate JWT
    - [ ] Configurable expireation
- [x] Validate JWT

## Protection

- [ ] UA Filter
- [ ] IP Filter
- [ ] CORS: https://github.com/lomigmegard/akka-http-cors
- [ ] DDOS: https://github.com/chatwork/akka-guard

## Caching

- [ ] Cache Flash API
- [x] Sitemap Caching
    - [ ] Configurable
- [x] ContentTypes Caching
    - [ ] Configurable
- Maybe I need others...

## Contents

- [x] Create
- [x] Select
    - [ ] Return id
    - [ ] GET API migrate from `path` to `id`
- [x] Update
    - [ ] Check diff & clean up externalResources & Tags
- [ ] Delete
    - [ ] Delete with externalResources & Tags

## Tags

- [x] Create
- [x] Select
- [x] Update
- [ ] Delete

## Search

- [ ] Create Table
- [ ] Select
    - [ ] By QueryParams

## Infrastracture

- [ ] Publish Docker Image

## Others

- [ ] RSS
- [x] Sitemap
- [ ] Robots.txt
    - implement with front end?
