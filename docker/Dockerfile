# Builder image
# docker buildx imagetools inspect ghcr.io/yoshinorin/docker-sbt:v1.12.12-scala3.8.4jdk25
FROM ghcr.io/yoshinorin/docker-sbt:v1.12.12-scala3.8.4jdk25@sha256:9380f87ae654468e7b95259a024f67d3b0662dc725aa76d78a4cb85b9220b4f1 as builder

LABEL maintainer="yoshinorin"
ENV QUALTET_VERSION="v3.2.0"

RUN apt update -y \
  && apt upgrade -y \
  && apt install --no-install-recommends -y git \
  && apt autoremove \
  && apt clean \
  && git clone --depth 1 --branch ${QUALTET_VERSION} https://github.com/yoshinorin/qualtet.git /usr/opt/qualtet

WORKDIR /usr/opt/qualtet
COPY commit-hash.sh commit-hash.sh

RUN sh commit-hash.sh \
  && sbt assembly exit \
  && cp ./target/scala-3.8.4/qualtet-assembly-${QUALTET_VERSION}.jar qualtet-assembly.jar


# Runtime image
# NOTE: need jdk image for inspect heapdump (e.g: need jstat etc)
# docker buildx imagetools inspect eclipse-temurin:25.0.3_9-jdk-noble
FROM eclipse-temurin:25.0.3_9-jdk-noble@sha256:02aba7518e48cfed96403ac9634e357a40329d6ec9418feb0b32636e43b245a1

WORKDIR /usr/opt/qualtet

LABEL maintainer="yoshinorin"

COPY --from=builder /usr/opt/qualtet/qualtet-assembly.jar .
COPY --from=builder /usr/opt/qualtet/qualtet.build_hash .
COPY entry-point.sh entry-point.sh

RUN apt update -y \
  && apt upgrade -y \
  && apt autoremove \
  && apt clean \
  && chmod +x entry-point.sh

ENTRYPOINT [ "/usr/opt/qualtet/entry-point.sh" ]
