# Base Image
FROM ubuntu:latest

# Label anstelle von MAINTAINER verwendet
LABEL maintainer="docker@thb.de"

# Installiert benötigte Pakete: Java 17
RUN apt-get update && \
    apt-get install -y openjdk-17-jre-headless curl

# Installiere Maven
RUN apt-get install -y maven

# Kopiert die JAR-Datei vom lokalen Build in das Image
ADD ./target/videothek-0.0.1-SNAPSHOT.jar /service.jar

# Exponiert den Port, auf dem die Anwendung läuft
EXPOSE 8080

# Setzt den Einstiegspunkt für den Container (Java-Anwendung starten)
ENTRYPOINT ["java", "-jar", "/service.jar"]
