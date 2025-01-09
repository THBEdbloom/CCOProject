# Wähle das Base Image
FROM ubuntu:latest

# Verwende ein Label anstelle von MAINTAINER (empfohlen)
LABEL maintainer="docker@thb.de"

# Installiere benötigte Pakete: Java 17 und Maven
RUN apt-get update && \
    apt-get install -y openjdk-17-jre-headless curl

# Installiere Maven (falls benötigt)
RUN apt-get install -y maven

# Kopiere die JAR-Datei von deinem lokalen Build in das Image
ADD ./target/videothek-0.0.1-SNAPSHOT.jar /service.jar

# Exponiere den Port, auf dem die Anwendung läuft
EXPOSE 8080

# Setze den Einstiegspunkt für den Container (Java-Anwendung starten)
ENTRYPOINT ["java", "-jar", "/service.jar"]
