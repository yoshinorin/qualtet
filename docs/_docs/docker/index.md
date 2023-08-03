---
layout: doc-page
title: Docker integration
---

## Docker Image

Qualtet provides docker image.

* On [GitHub Container Repository](https://github.com/yoshinorin/qualtet/pkgs/container/docker-qualtet)

## Example docker-compose.yml

Please see [docker-compose.yml](https://github.com/yoshinorin/qualtet/blob/master/docker/docker-compose.yml)

## Environment variables

As representative environment variables, following values are exists.

| Key | Value (Example) |
| --- | --- |
| `JAVA_VERSION` | `jdk-17.0.7+7` |
| `SBT_VERSION` | `1.9.0` |
| `QUALTET_VERSION` | `v2.7.0` |

If you want to see more details, please execute `docker inspect <image_id>`.
