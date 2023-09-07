FROM ghcr.io/yoshinorin/docker-sbt:v1.9.4-scala3

LABEL maintainer="yoshinorin"
ENV QUALTET_VERSION="v2.10.0"

RUN apt update -y \
  && apt upgrade -y \
  && apt install -y git vim \
  && apt autoremove \
  && apt clean

WORKDIR /usr/opt

RUN git clone --depth 1 --branch ${QUALTET_VERSION} https://github.com/yoshinorin/qualtet.git

WORKDIR /usr/opt/qualtet
COPY entry-point.sh entry-point.sh
COPY commit-hash.sh commit-hash.sh

RUN sh commit-hash.sh \
  && sbt assembly exit \
  && cp ./target/scala-3.3.1/qualtet-assembly-${QUALTET_VERSION}.jar qualtet-assembly.jar \
  && rm -rf scripts docs docker .github .gitignore .editorconfig .git .env .env.example Makefile .githooks target .scalafmt.conf tools \
  && chmod +x entry-point.sh

ENTRYPOINT [ "/usr/opt/qualtet/entry-point.sh" ]
