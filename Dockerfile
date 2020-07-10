FROM adoptopenjdk/openjdk11:alpine-jre
VOLUME /tmp
COPY target/LogframeLab-server-1.0.0-SNAPSHOT.jar .
EXPOSE 8080
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","LogframeLab-server-1.0.0-SNAPSHOT.jar"]