FROM ghcr.io/yoshinorin/docker-sbt:v1.8.0-scala3

LABEL maintainer="yoshinorin"

RUN apt update -y \
  && apt upgrade -y \
  && apt install -y git vim \
  && apt autoremove \
  && apt clean

WORKDIR /usr/opt

RUN git clone --depth 1 --branch v2.1.0 https://github.com/yoshinorin/qualtet.git

WORKDIR /usr/opt/qualtet

COPY entry-point.sh entry-point.sh

RUN rm -rf scripts docs docker .github .gitignore .editorconfig .git .env .env.example Makefile .githooks target .scalafix .scalafmt.conf \
  && sbt assembly exit \
  && cp ./target/scala-3.2.1/qualtet-assembly-v2.1.0.jar qualtet-assembly.jar \
  && chmod +x entry-point.sh

ENTRYPOINT [ "/usr/opt/qualtet/entry-point.sh" ]
