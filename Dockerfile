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

# JVM params
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", \
    "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", \
    "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", \
# start the jar on container start
    "-jar", "/app/epic-server.jar"]
