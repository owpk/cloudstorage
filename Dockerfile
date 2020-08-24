FROM maven:3.6-alpine as DEPS

WORKDIR /opt/app
COPY server.properties server.properties
COPY server/pom.xml server/pom.xml
COPY network/pom.xml network/pom.xml
COPY client/pom.xml client/pom.xml

COPY pom.xml .

FROM maven:3.6-alpine as BUILDER
WORKDIR /opt/app
COPY --from=deps /opt/app/ /opt/app
COPY server/src /opt/app/server/src
COPY network/src /opt/app/network/src
COPY client/src /opt/app/client/src

RUN mvn -pl .,network,server clean package -Dhttps.protocols=TLSv1.2

FROM openjdk:8-jre-alpine
WORKDIR /opt/app
COPY --from=builder /opt/app/server/target/server-1.0-jar-with-dependencies.jar .
COPY --from=builder /opt/app/server/target/server.properties .
EXPOSE 8190
CMD [ "java", "-jar", "server-1.0-jar-with-dependencies.jar" ]

