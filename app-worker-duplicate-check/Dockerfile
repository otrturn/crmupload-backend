FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# JAR aus dem Modul app-worker
COPY target/app-worker-duplicate-check*.jar app-worker-duplicate-check.jar

# Kein EXPOSE nötig, Worker hat kein HTTP
ENTRYPOINT ["java", "-jar", "app-worker-duplicate-check.jar"]
