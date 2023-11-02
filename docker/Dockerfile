FROM ghcr.io/yoshinorin/docker-sbt:v1.9.7.2-scala3

LABEL maintainer="yoshinorin"
ENV QUALTET_VERSION="v2.10.0"

RUN apt update -y \
  && apt upgrade -y \
  && apt install -y git \
  && apt autoremove \
  && apt clean \
  && git clone --depth 1 --branch ${QUALTET_VERSION} https://github.com/yoshinorin/qualtet.git /usr/opt/qualtet

WORKDIR /usr/opt/qualtet
COPY entry-point.sh entry-point.sh
COPY commit-hash.sh commit-hash.sh

RUN sh commit-hash.sh \
  && sbt assembly exit \
  && cp ./target/scala-3.3.1/qualtet-assembly-${QUALTET_VERSION}.jar qualtet-assembly.jar \
  && ls | grep -v -E 'qualtet-assembly.jar|entry-point.sh|commit-hash.sh|qualtet.build_hash' | xargs rm -r \
  && chmod +x entry-point.sh

ENTRYPOINT [ "/usr/opt/qualtet/entry-point.sh" ]
