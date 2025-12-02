FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# Maven-Build (falls du Multi-Stage mit maven willst, sonst vorher `mvn package`)
COPY target/app-web-*.jar app-web.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app-web.jar"]
