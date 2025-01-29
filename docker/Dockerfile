# Builder image
FROM ghcr.io/yoshinorin/docker-sbt:v1.10.7-scala3.6.3-jdk21 as builder

LABEL maintainer="yoshinorin"
ENV QUALTET_VERSION="v2.16.0"

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
  && cp ./target/scala-3.6.3/qualtet-assembly-${QUALTET_VERSION}.jar qualtet-assembly.jar


# Runtime image
# NOTE: need jdk image for inspect heapdump (e.g: need jstat etc)
FROM eclipse-temurin:21.0.4_7-jdk-noble

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
