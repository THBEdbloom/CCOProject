FROM ubuntu:latest
MAINTAINER docker@thb.de

RUN apt-get update
RUN apt-get install openjdk-17-jre-headless -y

# Maven
ADD ./target/videothek-0.0.1-SNAPSHOT.jar /service.jar

# Setze Umgebungsvariablen für die Datenbankverbindung (optional)
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/videothek
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=Test_12345!?

# Exponiere den Port, auf dem die Anwendung läuft
EXPOSE 8080

ENTRYPOINT java -jar /service.jar
EXPOSE 8080
