#!/bin/sh
# Wrapper Script to start Server Jar and remove old files.

# JVM params
java -server -XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap -XX:InitialRAMFraction=2 -XX:MinRAMFraction=2 \
-XX:MaxRAMFraction=2 -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication \
-jar /app/epic-server.jar &

# Remove files older than 5 minutes
while true
  do
    find /storage -type d -mmin +5 -exec rm -r {} ';' 2>/dev/null
  sleep 60
done