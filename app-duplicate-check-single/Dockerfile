FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/app-duplicate-check-single*.jar /app/app-duplicate-check-single.jar

ENTRYPOINT ["java","-jar","/app/app-duplicate-check-single.jar"]
