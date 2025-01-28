---
layout: doc-page
title: Docker integration
---

## Docker Image

Qualtet provides a Docker image.

* On [GitHub Container Repository](https://github.com/yoshinorin/qualtet/pkgs/container/docker-qualtet)

## Example docker-compose.yml

Please see [docker-compose.yml](https://github.com/yoshinorin/qualtet/blob/master/docker/docker-compose.example.yml)

## Environment variables

The following are representative environment variables.

| Key | Value (Example) |
| --- | --- |
| `JAVA_VERSION` | `jdk-17.0.7+7` |
| `SBT_VERSION` | `1.9.0` |
| `QUALTET_VERSION` | `v2.16.0` |

If you want to see more details, please execute `docker inspect <image_id>`.
