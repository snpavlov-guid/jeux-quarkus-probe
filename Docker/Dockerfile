FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY JeuxDBContext/pom.xml JeuxDBContext/pom.xml
COPY JeuxWebAPI/pom.xml JeuxWebAPI/pom.xml

COPY JeuxDBContext/src JeuxDBContext/src
COPY JeuxWebAPI/src JeuxWebAPI/src
COPY JeuxWebAPI/config/application.properties JeuxWebAPI/src/main/resources/application.properties

RUN cat JeuxWebAPI/src/main/resources/application.properties | grep "oidc.auth-server-url"
RUN rm -rf JeuxWebAPI/target JeuxDBContext/target
RUN mvn -f JeuxDBContext/pom.xml clean install -DskipTests
RUN mvn -f JeuxWebAPI/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app/quarkus-app

COPY --from=build /workspace/JeuxWebAPI/target/quarkus-app/ ./

ENV QUARKUS_HTTP_PORT=19081

EXPOSE 19081

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
