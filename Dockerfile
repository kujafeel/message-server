FROM gradle:7.6.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle fatJar

FROM openjdk:17
WORKDIR /app
COPY --from=build /app/build/libs/message-server-all.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "message-server-all.jar"]