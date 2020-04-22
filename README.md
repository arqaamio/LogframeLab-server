# LogframeLab-server

Logframe Lab is a project of Arqaam which anticipates to fill two gaps. On the one hand, it shall support the creation of more standardised logframes from implementing partners, which will in turn lead to more standardised data and thus more comparability of projects. On the other hand, it can help development actors to learn more about logframes and indicators, and how to choose good indicators for their respective projects.


## 1- Install maven

## 2- Install docker
https://docs.docker.com/get-docker/

## 3- Install docker-compose
https://docs.docker.com/compose/install/

## 4- Open the current project directory
cd LogframeLab-server

## 5- Run mysql and phpMyAdmin
docker-compose up -d

## 6- Run database flyway migrate 
mvn clean flyway:migrate -Dflyway.configFile=flywayConfig.properties

## 7- Run logframelab spring boot application (profiles:dev swagger,  port:8082)
mvn spring-boot:run -Dspring-boot.run.profiles=dev,swagger

## phpMyAdmin (database:arqaam)
phpmyadmin: http://127.0.0.1:8080/

## Swagger
You can find the API documentation at http://127.0.0.1:8082/swagger-ui.html
=======
# How to contribute

## 1- Fork the project

## 2- Clone the forked project
git clone https://github.com/username/repository.git

## 3- open the project directory
cd LogframeLab-server

## 4- Sync the loacl forked repository with the main repo 
git remote add upstream https://github.com/arqaamio/LogframeLab-server.git
git fetch upstream

## 5- Check the updated develop branch from the main repo
git checkout upstream/develop

## 6- Copy the develop branch into a new feature branch
git checkout -b feature/<feature_name>

## 7- Develop the new feature and commit the changes
git add .
git commit -m "<feature_id_from_task_managment_system> feature description"

## 8- Push the new feature/<feature_name> to the forked repository
git push --set-upstream origin feature/<feature_name>

## 9- Select the feature/<feature_name> from the forked repository in github

## 10- Pull request the new feature/<feature_name> branch against the develop branch in the main repository 

[![License: CC BY-NC-SA 4.0](https://licensebuttons.net/l/by-nc-sa/4.0/80x15.png)](https://creativecommons.org/licenses/by-nc-sa/4.0/)
