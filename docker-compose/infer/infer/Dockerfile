FROM debian:stretch-slim

LABEL maintainer "Infer team"

# mkdir the man/man1 directory due to Debian bug #863199
RUN echo "deb http://deb.debian.org/debian stretch-backports main" >> /etc/apt/sources.list && \
    apt-get update && \
    mkdir -p /usr/share/man/man1 && \
    apt-get install --yes --no-install-recommends \
      autoconf \
      automake \
      cmake \
      curl \
      git \
      less \
      libc6-dev \
      libsqlite3-dev \
      opam \
      openjdk-8-jdk-headless \
      pkg-config \
      python2.7 \
      zlib1g-dev && \
    apt-get -t stretch-backports install --yes --no-install-recommends openjdk-11-jdk-headless

RUN VERSION=0.17.0; \
    curl -sSL "https://github.com/facebook/infer/releases/download/v$VERSION/infer-linux64-v$VERSION.tar.xz" \
    | tar -C /opt -xJ && \
    find /opt/infer-linux64-v$VERSION/bin/* -exec ln -s {} /usr/local/bin \;
