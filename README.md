# LogframeLab-server

Logframe Lab is a project of Arqaam which anticipates to fill two gaps. On the one hand, it shall support the creation of more standardised logframes from implementing partners, which will in turn lead to more standardised data and thus more comparability of projects. On the other hand, it can help development actors to learn more about logframes and indicators, and how to choose good indicators for their respective projects.


## 1- Install maven

## 2- Install docker
https://docs.docker.com/get-docker/

## 3- Install docker-compose
https://docs.docker.com/compose/install/

## 4- Open the current project directory
```sh
cd LogframeLab-server
```

## 5- Run mysql and phpMyAdmin
```sh
docker-compose up --build -d db phpmyadmin
```

## 6- Run logframelab spring boot application (profiles:dev,  port:8080)
```sh
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## phpMyAdmin (database:arqaam)
phpmyadmin: http://127.0.0.1:8080/

## Swagger
You can find the API documentation at http://127.0.0.1:8080/swagger-ui.html

# How to contribute

## 1- Fork the project

## 2- Clone the forked project
```sh
git clone https://github.com/username/repository.git
```

## 3- open the project directory
```sh
cd LogframeLab-server
```

## 4- Sync the local forked repository with the main repo 
```sh
git remote add upstream https://github.com/arqaamio/LogframeLab-server.git
git fetch upstream
```

## 5- Check the updated develop branch from the main repo
```sh
git checkout upstream/develop
```

## 6- Copy the develop branch into a new feature branch
```sh
git checkout -b feature/<feature_name>
```

## 7- Develop the new feature and commit the changes
```sh
git add .
git commit -m "<feature_id_from_task_managment_system> feature description"
```

## 8- Push the new feature/<feature_name> to the forked repository
```sh
git push --set-upstream origin feature/<feature_name>
```

## 9- Select the feature/<feature_name> from the forked repository in github

## 10- Pull request the new feature/<feature_name> branch against the develop branch in the main repository 

## Running Integration Tests
In order to run the integration tests that require the actual beans to be used in Dependency Injection locally, do the following:
* ~~Manually create the __*integration_tests*__ database with __root__ user and password __root__. These credentials align with the docker config.~~
* Make sure docker is running. 
* Run the usual *mvn test* command. 
* At runtime, [Testcontainers](https://www.testcontainers.org/test_framework_integration/junit_5/)
 will deploy a MySQL instance as specified in the `BaseDatabaseTest` interface. Secondly, `BaseDatabaseTest.Initializer` will inject
 the deployed MySQL url into the __application-integration.properties__ file where needed.
* The tests will proceed as usual. 

[![License: CC BY-NC-SA 4.0](https://licensebuttons.net/l/by-nc-sa/4.0/80x15.png)](https://creativecommons.org/licenses/by-nc-sa/4.0/)
