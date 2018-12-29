#FROM adoptopenjdk/openjdk8-openj9:jdk8u192-b12_openj9-0.11.0-alpine-slim as builder
FROM maven:3.6.0-jdk-8-slim as builder

WORKDIR /usr/src/app

COPY pom.xml .
RUN mvn clean package

COPY . .
RUN mvn package

FROM tomee:8-jre-8.0.0-M1-plus

RUN rm -rf /usr/local/tomee/webapps/*

COPY --from=builder /usr/src/app/target/ROOT.war /usr/local/tomee/webapps/
#COPY target/ROOT.war /usr/local/tomee/webapps/
