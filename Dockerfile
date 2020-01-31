# Docker image with preinstalled JVM
FROM openjdk:8-jre-alpine

# cd
WORKDIR /opt

RUN apk update && \
    apk add curl && \
    apk add texlive && \

    # install pandoc
    curl -L "https://github.com/jgm/pandoc/releases/download/2.9.1.1/pandoc-2.9.1.1-linux-amd64.tar.gz" -o pandoc.tar.gz && \
    tar xzvf pandoc.tar.gz && \
    rm pandoc.tar.gz -f && \

    # alias in /opt for simplicity
    ln -sT "pandoc-2.9.1.1/bin/pandoc" "pandoc" && \

    mkdir /storage

# copy server jar to image
COPY ./build/libs/epic-server.jar /app/epic-server.jar

# Run start script
COPY ./start.sh /start.sh
RUN ["chmod", "+x", "/start.sh"]
CMD  ["/bin/sh", "-c", "/start.sh"]
