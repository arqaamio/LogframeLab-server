# Logframe Lab

Logframe Lab is an open source web application created to help NGOs and community-based organisations match their project ideas with the most relevant metrics for tracking their success. This tool leads you step-by-step through a simple process that results in a formatted, logical framework (logframe) that you can download and use for reporting your progress to your funders.

Its live at https://logframelab.ai

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Installing Dependencies
1. Install [Maven] https://maven.apache.org/install.html
2. Install [Docker] https://docs.docker.com/get-docker/
3. Install [docker-compose] https://docs.docker.com/compose/install/

### Quick Start
1. Fork the repository 
2. Git clone the repository (Replace `YOUR-USERNAME`)
```sh
$ git clone https://github.com/YOUR-USERNAME/LogframeLab-server
```
3. Start the database and database manager containers
```sh
$ docker-compose up --build -d db phpmyadmin
```
4. Run the application with the dev profile (It becomes available at localhost:8080)
```sh
$ mvn spring-boot:run
```

The database manager is available at http://localhost:8082/.
We use [Swagger] for API documentation which is available at http://localhost:8080/swagger-ui.html

## Contributing
Want to help build on Logframe Lab? Check out our documentation [here](CONTRIBUTING.md).

## License

Logframe Lab is licensed under the terms of the Creative Commons Attribution Share Alike 4.0 license, which can be read in full [here](LICENSE).

## Maintainers
Ari is available at: ari (dot) xxxx (at) protonmail (dot) com

[![License: CC BY-NC-SA 4.0](https://licensebuttons.net/l/by-nc-sa/4.0/80x15.png)](https://creativecommons.org/licenses/by-nc-sa/4.0/)

[Docker]: <https://www.docker.com/>
[docker-compose]: <https://docs.docker.com/compose/>
[Maven]: <https://maven.apache.org>
[MySQL]: <https://www.mysql.com/>
[PhpMyAdmin]: <https://www.phpmyadmin.net/>
[Swagger]: <https://swagger.io/>