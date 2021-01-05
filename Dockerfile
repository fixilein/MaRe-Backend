# Docker image with preinstalled JVM
FROM openjdk:8-jre-alpine

# cd
WORKDIR /opt

RUN echo  >> /etc/apk/repositories

RUN apk update && \
    apk add curl && \
    apk add font-noto-cjk-extra --repository http://dl-cdn.alpinelinux.org/alpine/edge/community

RUN apk add texlive-full

# install pandoc
RUN curl -L "https://github.com/jgm/pandoc/releases/download/2.11.2/pandoc-2.11.2-linux-amd64.tar.gz" -o pandoc.tar.gz && \
    tar xzvf pandoc.tar.gz && \
    rm pandoc.tar.gz -f && \
    # alias in /opt for simplicity
    ln -sT "pandoc-2.11.2/bin/pandoc" "pandoc"

RUN apk add font-noto
RUN apk add font-noto-devanagari
RUN apk add font-noto-myanmar
RUN apk add font-noto-arabic
RUN apk add font-noto-hebrew
RUN apk add font-noto-bengali

RUN mkdir /storage

# copy server jar to image
COPY ./build/libs/epic-server.jar /app/epic-server.jar

# Run start script
COPY ./start.sh /start.sh
COPY ./make.sh /make.sh
RUN ["chmod", "+x", "/start.sh"]
RUN ["chmod", "+x", "/make.sh"]
CMD ["/bin/sh", "-c", "/start.sh"]
