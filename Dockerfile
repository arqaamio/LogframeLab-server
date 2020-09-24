FROM adoptopenjdk/openjdk11:alpine-jre
VOLUME /tmp
COPY target/LogframeLab-server-1.0.0-SNAPSHOT.jar .
EXPOSE 8080
ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005","-jar","-Dspring.profiles.active=prod","LogframeLab-server-1.0.0-SNAPSHOT.jar"]
RUN apk update && apk add mysql-client && rm -rf /var/lib/apt