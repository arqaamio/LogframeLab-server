FROM adoptopenjdk/openjdk11:alpine-jre
ARG VERSION
VOLUME /tmp
COPY target/LogframeLab-server-$VERSION.jar LogframeLab-server.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","LogframeLab-server.jar"]
