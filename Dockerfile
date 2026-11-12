# One deployable image: the Angular build is served by Spring Boot from the same origin as the API.

FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build --configuration production

FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn -q dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /app/dist/frontend/browser ./src/main/resources/static
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/target/hr-salary-management-*.jar app.jar
# The SQLite file lives on a volume so data survives restarts; it is seeded on first start.
ENV DB_PATH=/data/hr.db
VOLUME /data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
