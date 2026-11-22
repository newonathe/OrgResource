
# Step 1: Build the React frontend
FROM node:24-bookworm-slim AS frontend-build

WORKDIR /workspace

COPY extended/frontend/package.json extended/frontend/package-lock.json extended/frontend/

RUN npm --prefix extended/frontend ci

COPY extended/frontend/ extended/frontend/

RUN npm --prefix extended/frontend run build

# Step 2: Build the Java 21 backend
FROM maven:3.9-eclipse-temurin-21 AS java-build

WORKDIR /workspace

COPY extended/backend/pom.xml extended/backend/pom.xml
COPY extended/backend/src/ extended/backend/src/

COPY --from=frontend-build /workspace/extended/frontend/dist/ /workspace/extended/frontend/dist/

RUN mvn -B -f extended/backend/pom.xml -DskipTests clean package

# Step 3: Run the combined application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=java-build /workspace/extended/backend/target/extended-backend-0.1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
