# How to contribute
Thank you for considering contributing to Logframe Lab$

Below are the guidelines to help you understand the most efficient way for you to contribute your ideas to our project.

While we have done our best to guide you through any questions that might come up, we realize this document might not be comprehensive. 

We ask that you use your best judgement in the kinds of pull requests you make.

Please note that we expect contributors to adhere to our Code of Conduct at all times.

 - [Code of Conduct](#code-of-conduct)
 - [Questions or Problems?](#questions-or-problems)
 - [Found a Bug?](#found-a-bug)
 - [Missing a Feature?](#missing-a-feature)
 - [Submitting an Issue](#submitting-an-issue)
 - [The Development Environment](#the-development-environment)
 - [Submitting a Pull Request (PR)](#submitting-a-pull-request-pr)
 

## Code of Conduct
It is essential that our community is an open and inclusive place to share ideas. Our [Code of Conduct](CODE_OF_CONDUCT.md) makes clear the standards that we hold ourselves and our community to.

## Questions or Problems?
Do not open issues for general support questions, as we want to keep GitHub issues for bug reports and feature requests. Please be sure to review all aspects of our FAQ page found [here](https://logframelab.tawk.help/).
 
If you have further questions or issues you would like to report for something that is not covered by the above sources, you can reach out to ari.xxxx@protonmail.com

## Found a Bug?
If you find a bug in the source code, you can help us by submitting an issue to our GitHub Repository. Even better, you can submit a Pull Request with a fix.

## Missing a Feature?
You can request a new feature by submitting an issue to our GitHub Repository. If you would like to implement a new feature, please submit an issue with a for your work first, to be sure that we can use it. Please consider what kind of change it is:

For a Major Feature, first open an issue and outline your proposal so that it can be discussed. This will also allow us to better coordinate our efforts, prevent duplication of work, and help you to craft the change so that it is successfully accepted into the project.
Small Features can be crafted and directly submitted as a Pull Request.

## Submitting an Issue
Before you submit an issue, please search the issue tracker to check if the problem already exists for others. The discussion might inform you of workarounds readily available.

## The Development Environment
To run a local environment successfully you will need [Maven], [Docker] and [docker-compose]. Using [docker-compose] containers will be created to run the local database using [MySQL] and database manager [PhpMyAdmin]. A volume will also be created with the database at `docker` folder. If you wish to restart the database you can remove the folder and build the containers.

After installing it and forking the project to your local machine, run:
```sh
docker-compose up --build -d db phpmyadmin
```
And now you can run the application:
```sh
mvn spring-boot:run
```

## Submitting a Pull Request (PR)
Before you submit your Pull Request (PR) consider the following guidelines:
 - Search GitHub for an open or closed PR that relates to your submission. You don't want to duplicate effort.
 - Fork the project
 - Clone the repository
```sh
$ git clone
```
 - Make your changes in a new git branch:
```sh
	$ git checkout -b my-fix-branch master
```
 - Create your patch, including appropriate test cases.
 - Make sure the application is valid and the test are successfull.
 ```sh
 mvn clean install
 ```
 - Commit your changes using a descriptive commit message.
```sh
$ git commit -a
```
> Note: the optional commit -a command line option will automatically "add" and "rm" edited files.
Push your branch to GitHub:
```sh
$ git push origin my-fix-branch
```
 - In GitHub, send a pull request to `logframelab:master`.
 - If we suggest changes then:
   - Make the required updates.
   - Re-run the Logframe Lab test suites to ensure tests are still passing.

Rebase your branch and force push to your GitHub repository (this will update your Pull Request):
```sh
git rebase master -i
git push -f
```
That's it! Thank you for your contribution!
 
### Where can I get more help, if I need it?

Contact the maintainers: sheena.amdadoo@gmail.com and yiriwayah@gmail.com

[Docker]: <https://www.docker.com/>
[docker-compose]: <https://docs.docker.com/compose/>
[Maven]: <https://maven.apache.org>
[MySQL]: <https://www.mysql.com/>
[PhpMyAdmin]: <https://www.phpmyadmin.net/>
