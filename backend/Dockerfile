# --- Etape 1 : build du jar avec Maven + JDK 21 ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- Etape 2 : image d'execution legere (JRE 21) ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/covoiturage-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
