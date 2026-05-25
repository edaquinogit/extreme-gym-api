FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

ARG SKIP_TESTS=true

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src

RUN chmod +x mvnw \
    && if [ "$SKIP_TESTS" = "true" ]; then \
        ./mvnw -B -DskipTests package; \
    else \
        ./mvnw -B test package; \
    fi

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
