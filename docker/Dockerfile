# Builder image
# docker buildx imagetools inspect ghcr.io/yoshinorin/docker-sbt:v2.0.10-scala3.9.0jdk25
FROM ghcr.io/yoshinorin/docker-sbt:v2.0.10-scala3.9.0jdk25@sha256:3e57011a0214fc4a8e135ebe65e89eb91f98d06f88760b7fe56ca71bfc8b8be2 as builder

LABEL maintainer="yoshinorin"
ENV QUALTET_VERSION="v3.4.0"

RUN apt update -y \
  && apt upgrade -y \
  && apt install --no-install-recommends -y git \
  && apt autoremove \
  && apt clean

WORKDIR /usr/opt/qualtet
COPY . .

RUN sh docker/commit-hash.sh \
  && sbt assembly exit \
  && cp ./target/out/jvm/scala-3.9.0/qualtet/qualtet-assembly-${QUALTET_VERSION}.jar qualtet-assembly.jar


# Runtime image
# docker buildx imagetools inspect eclipse-temurin:25.0.4.1_1-jre-resolute
FROM eclipse-temurin:25.0.4.1_1-jre-resolute@sha256:41d9263c0d5b2a74d89af6e29e73b04298bc65e3cb705d03e9e2271ef49fef84

WORKDIR /usr/opt/qualtet

LABEL maintainer="yoshinorin"

COPY --from=builder /usr/opt/qualtet/qualtet-assembly.jar .
COPY --from=builder /usr/opt/qualtet/qualtet.build_hash .
COPY docker/entry-point.sh entry-point.sh

RUN apt update -y \
  && apt upgrade -y \
  && apt autoremove \
  && apt clean \
  && chmod +x entry-point.sh

ENTRYPOINT [ "/usr/opt/qualtet/entry-point.sh" ]
