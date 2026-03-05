FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies -q
COPY src ./src
RUN gradle bootJar -x test -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Install docker CLI for Wings container restart capability
RUN apk add --no-cache docker-cli

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
