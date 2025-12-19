FROM eclipse-temurin:21-jre
WORKDIR /app

# /data/tmp optional anlegen (nicht zwingend, aber nett)
RUN mkdir -p /data/tmp

COPY target/*.jar /app/app-billing.jar
ENTRYPOINT ["java","-jar","/app/app-billing.jar"]