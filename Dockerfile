FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradlew .
COPY gradle/ ./gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x ./gradlew

COPY src/ ./src

RUN ./gradlew bootJar --no-daemon


FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

EXPOSE 8080

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]