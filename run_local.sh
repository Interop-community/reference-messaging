#!/usr/bin/env bash

# is already running?
#if (nc -z localhost 8091; echo $?)

echo "running..."
java -jar reference-messaging/target/hspc-reference-messaging-*.jar
