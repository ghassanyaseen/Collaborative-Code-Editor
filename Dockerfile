FROM openjdk:17-jdk-slim

RUN apt-get update && \
    apt-get install -y docker.io && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/CollaborativeCodeEditor-0.0.1-SNAPSHOT.jar CollaborativeCodeEditor-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "CollaborativeCodeEditor-0.0.1-SNAPSHOT.jar"]
