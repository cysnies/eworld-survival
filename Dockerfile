FROM eclipse-temurin:8-jre-jammy

RUN apt-get update \
  && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends perl \
  && rm -rf /var/lib/apt/lists/*

COPY docker/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh \
  && chmod +x /usr/local/bin/docker-entrypoint.sh

RUN mkdir -p /server

WORKDIR /server

EXPOSE 25565 25575

ENV LANG=C.UTF-8 \
  LC_ALL=C.UTF-8 \
  SERVER_ROOT=/server \
  MYSQL_HOST=mysql \
  MYSQL_PORT=3306 \
  MYSQL_DATABASE=mc001 \
  MYSQL_USER=mc001 \
  MYSQL_PASSWORD=changeme \
  JAVA_OPTS="-Xms512M -Xmx2048M -Dfile.encoding=UTF-8" \
  FIX_CONTAINER_BIND=1

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["nogui"]
