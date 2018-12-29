#!/bin/sh

docker build --tag kotlin-cdi-jaxrs:latest .

docker run -it --rm \
    --memory 128M \
    --publish 8080:8080 \
    --name kotlin-cdi-jaxrs \
    kotlin-cdi-jaxrs:latest

