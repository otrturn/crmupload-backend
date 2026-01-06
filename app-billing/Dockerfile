FROM eclipse-temurin:21-jre
WORKDIR /app

RUN mkdir -p /data/tmp /data/invoices

COPY target/*.jar /app/app-billing.jar
ENTRYPOINT ["java","-jar","/app/app-billing.jar"]