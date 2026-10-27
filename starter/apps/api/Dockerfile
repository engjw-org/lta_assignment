# Build context: starter root (so the data/ folder is reachable)
# Build: docker build -f apps/api/Dockerfile -t vrls2-api .

FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY apps/api/pom.xml .
RUN mvn -B dependency:go-offline
COPY apps/api/src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
COPY data /data
ENV VRLS_DATA_DIR=/data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
