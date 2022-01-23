FROM ghcr.io/yoshinorin/docker-sbt:v1.6.1

MAINTAINER yoshinorin

RUN apt-get update -y \
  && apt-get upgrade -y \
  && apt-get install -y git vim \
  && apt-get autoremove \
  && apt-get clean

WORKDIR /usr/opt

# TODO: download latest from GitHub releases after first release.
RUN git clone https://github.com/yoshinorin/qualtet.git

WORKDIR /usr/opt/qualtet

COPY entry-point.sh entry-point.sh

RUN rm -rf scripts docs docker .github .gitignore .editorconfig .git .env .env.example \
  && sbt assembly exit \
  && cp ./target/scala-2.13/qualtet-assembly-1.0.0-SNAPSHOT.jar qualtet-assembly.jar \
  && chmod +x entry-point.sh

ENTRYPOINT [ "/usr/opt/qualtet/entry-point.sh" ]
