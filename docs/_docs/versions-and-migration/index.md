---
layout: doc-page
title: Versions and Migration State
---

## About

The state and its transition diagram for application version management and migration is shown below. This feature was introduced in `v2.18.0`.

The information is managed in the `versions` table in database, and migrations are performed at application startup depending on the current state.

**NOTE:** This migration sequence is managed independently of Flyway and runs as a separate process.

## State

| State | Description |
|---|---|
| `not_required` | Migration was not required for this version. |
| `unapplied` | Migration for this version has not been applied. |
| `in_progress` | Migration is in progress. |
| `success` | Migration completed successfully. |
| `failed` | Migration failed. Some errors may occur in the running application. Migration will be retried on the next application startup. |

## Transition Diagram

![](./assets/migration_state.png)
