FROM maven:3.9.9-eclipse-temurin-23

WORKDIR /workspace

RUN groupadd --system test \
    && useradd --system --gid test --create-home --home-dir /home/test test \
    && mkdir -p /workspace/target /workspace/allure-results /tmp/maven \
    && chown -R test:test /workspace /home/test /tmp/maven

COPY pom.xml .
RUN chown test:test pom.xml

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod 755 /usr/local/bin/docker-entrypoint.sh

USER test

ENV MAVEN_CONFIG=/tmp/maven

RUN /usr/local/bin/docker-entrypoint.sh -B dependency:go-offline
COPY src ./src

ENTRYPOINT ["docker-entrypoint.sh"]
