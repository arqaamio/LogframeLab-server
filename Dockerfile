FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/LogframeLab-server-1.0.0-SNAPSHOT.jar .
EXPOSE 8080
ENTRYPOINT ["java","-jar","LogframeLab-server-1.0.0-SNAPSHOT.jar"]