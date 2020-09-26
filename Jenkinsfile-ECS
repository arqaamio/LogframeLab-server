def KEEP_IMAGE = true // whether you wnat your iamge to be pushed to Dockerhub or not. Image gets pushed by default for develop and master branches.
def devops_repo_branch = "master" // Branch of teh devops repository. Default is master.
def appName = 'LogframeLab' // Name of the app used in Terraform.

/* ENVIRONMENT BRANCHES used to Deploy*/
environment_branches = ['develop', 'master'] 

pipeline {
    agent any
    tools {
        maven 'Maven'
    }
    stages {
        stage('Build') {
            steps {
                script{
                    sh 'mvn -DskipTests clean package'
                }
            }
        }
        
        stage('Test') {
            steps {
                // sh 'echo Test'
                sh 'mvn test'
            }
            post {
                // If Maven was able to run the tests, even if some of the test
                // failed, record the test results and archive the jar file.
                success {
                    junit '**/target/surefire-reports/TEST-*.xml'
                    // archiveArtifacts 'target/*.jar'
                }
            }
        }

        stage ('Build Docker Image') {
            steps {
                script {
                    def dockerfile = "${env.WORKSPACE}/Dockerfile"

                    // Read version from pom.xml
                    env.image_version = sh script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true

                    // Determine Image Tag
                    GIT_HASH = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
                    def commit_id = GIT_HASH
                    env.image_name = 'server'
                    env.image_tag = "${env.BRANCH_NAME != 'master' || env.BRANCH_NAME != 'develop' ? 'feature-' + commit_id : env.BUILD_NUMBER}"
                    sh "docker build -t ${image_name}:${image_version}-${image_tag} --build-arg VERSION=${env.image_version} ."
                }
            }
        }

        stage ('Tag and Push Docker Image') {
            when {
                expression {
                    return env.BRANCH_NAME in environment_branches || KEEP_IMAGE == true;
                    // Run only for stable branches and not PRs
                }
            }
            steps {
                script {
                    if (env.BRANCH_NAME == 'develop') {
                        sh "docker tag ${env.image_name}:${ienv.mage_version}-${env.image_tag} logframelab/${env.image_name}:${env.image_version}-${env.image_tag}"
                        sh "docker tag ${env.image_name}:${env.image_version}-${env.image_tag} logframelab/${env.image_name}:devlatest"
                        withDockerRegistry(url: 'https://index.docker.io/v1/',  credentialsId: 'dockerhub') {
                            sh "docker push logframelab/${env.image_name}:${env.image_version}-${env.image_tag}"
                            sh "docker push logframelab/${env.image_name}:devlatest"
                        }

                        // Set Deployment variables
                        env.TERRAFORM_ENVIRONMENT = 'dev'
                        env.CLIENT_IMAGE_TAG = 'devlatest'
                        env.SERVER_IMAGE_TAG = 'devlatest'
                    }

                    if (env.BRANCH_NAME == 'master') {
                        sh "docker tag ${env.image_name}:${env.image_version}-${env.image_tag} logframelab/${env.image_name}:${env.image_version}-${env.image_tag}"
                        sh "docker tag ${env.image_name}:${env.image_version}-${env.image_tag} logframelab/${env.image_name}:stagelatest"
                        withDockerRegistry(url: 'https://index.docker.io/v1/',  credentialsId: 'dockerhub') {
                            sh "docker push logframelab/${env.image_name}:${env.image_version}-${env.image_tag}"
                            sh "docker push logframelab/${env.image_name}:stagelatest"
                        }
                        // Set Deployment variables
                        env.TERRAFORM_ENVIRONMENT = 'stage'
                        env.CLIENT_IMAGE_TAG = 'stagelatest'
                        env.SERVER_IMAGE_TAG = 'stagelatest'
                    }

                    if (KEEP_IMAGE == true) {
                        sh "docker tag ${env.image_name}:${env.image_version}-${env.image_tag} logframelab/${env.image_name}:${env.image_version}-${env.image_tag}"
                        withDockerRegistry(url: 'https://index.docker.io/v1/',  credentialsId: 'dockerhub') {
                            sh "docker push logframelab/${env.image_name}:${env.image_version}-${env.image_tag}"
                        }
                    }
                }
            }
        }

        stage ('Invoke Terraform: Provisioner:base_resources, Action:plan') {
            when {
                expression {
                    return env.BRANCH_NAME in environment_branches;
                    // Run only for stable branches and not PRs
                }
            }
            steps {
                build job: 'Deployments/Terraform-Deployment-Pipeline',
                wait: true,
                parameters: [
                    string(name: 'ENV', value: env.TERRAFORM_ENVIRONMENT),
                    string(name: 'ACTION', value: "plan"),
                    string(name: 'PROVISIONER', value: "base-resources"),
                    string(name: 'BRANCH', value: devops_repo_branch),
                    string(name: 'TF_VAR_server_version', value: env.image_version),
                    string(name: 'TF_VAR_client_image_tag', value: env.CLIENT_IMAGE_TAG),
                    string(name: 'TF_VAR_server_image_tag', value: env.SERVER_IMAGE_TAG),
                    credentials(name: 'DB_PASSWORD',value: 'TF-' + env.TERRAFORM_ENVIRONMENT + '-mysql_password')
                ]
            }
        }

        stage ('Invoke Terraform: Provisioner:base_resources , Action:apply') {
            when {
                expression {
                    return env.BRANCH_NAME in environment_branches;
                    // Run only for stable branches and not PRs
                }
            }
            steps {
                build job: 'Deployments/Terraform-Deployment-Pipeline',
                wait: true,
                parameters: [
                    string(name: 'ENV', value: env.TERRAFORM_ENVIRONMENT),
                    string(name: 'ACTION', value: "apply"),
                    string(name: 'PROVISIONER', value: "base-resources"),
                    string(name: 'BRANCH', value: devops_repo_branch),
                    string(name: 'TF_VAR_server_version', value: env.image_version),
                    string(name: 'TF_VAR_client_image_tag', value: env.CLIENT_IMAGE_TAG),
                    string(name: 'TF_VAR_server_image_tag', value: env.SERVER_IMAGE_TAG),
                    credentials(name: 'DB_PASSWORD',value: 'TF-' + env.TERRAFORM_ENVIRONMENT + '-mysql_password')
                ]
            }
        }

        stage ('Invoke Terraform: Provisioner:ecs , Action:plan') {
            when {
                expression {
                    return env.BRANCH_NAME in environment_branches;
                    // Run only for stable branches and not PRs
                }
            }
            steps {
                build job: 'Deployments/Terraform-Deployment-Pipeline',
                wait: true,
                parameters: [
                    string(name: 'ENV', value: env.TERRAFORM_ENVIRONMENT),
                    string(name: 'ACTION', value: "plan"),
                    string(name: 'PROVISIONER', value: "ecs"),
                    string(name: 'BRANCH', value: devops_repo_branch),
                    string(name: 'TF_VAR_server_version', value: env.image_version),
                    string(name: 'TF_VAR_client_image_tag', value: env.CLIENT_IMAGE_TAG),
                    string(name: 'TF_VAR_server_image_tag', value: env.SERVER_IMAGE_TAG),
                    credentials(name: 'DB_PASSWORD',value: 'TF-' + env.TERRAFORM_ENVIRONMENT + '-mysql_password')
                ]
            }
        }

        stage ('Invoke Terraform: Provisioner:ecs , Action:apply') {
            when {
                expression {
                    return env.BRANCH_NAME in environment_branches;
                    // Run only for stable branches and not PRs
                }
            }
            steps {
                build job: 'Deployments/Terraform-Deployment-Pipeline',
                wait: true,
                parameters: [
                    string(name: 'ENV', value: env.TERRAFORM_ENVIRONMENT),
                    string(name: 'ACTION', value: 'apply'),
                    string(name: 'PROVISIONER', value: "ecs"),
                    string(name: 'BRANCH', value: devops_repo_branch),
                    string(name: 'TF_VAR_server_version', value: env.image_version),
                    string(name: 'TF_VAR_client_image_tag', value: env.CLIENT_IMAGE_TAG),
                    string(name: 'TF_VAR_server_image_tag', value: env.SERVER_IMAGE_TAG),
                    credentials(name: 'DB_PASSWORD',value: 'TF-' + env.TERRAFORM_ENVIRONMENT + '-mysql_password')
                ]
            }
        }

        stage ('Stop Existing tasks and wait until service is stable') {
            when {
                expression {
                    return env.BRANCH_NAME in environment_branches;
                    // Run only for stable branches and not PRs
                }
            }
            steps {
                sh """
                    tasks=\$(aws ecs list-tasks \
                    --cluster ${appName}-${env.TERRAFORM_ENVIRONMENT}-ecs-cluster \
                    --service-name ${appName}-${env.TERRAFORM_ENVIRONMENT}-service \
                    --region eu-west-1 | jq -r '.taskArns[]')

                    echo "Stopping currently running tasks"
                    for task in \$tasks
                    do
                        echo "Stopping running task: \$task"
                        aws ecs stop-task --cluster ${appName}-${env.TERRAFORM_ENVIRONMENT}-ecs-cluster \
                        --reason "Stopped by Deployment Job" \
                        --task \$task \
                        --region eu-west-1

                        if [ \$? -eq 0 ]; then
                            echo "\$task stopped successfully"
                        else
                            echo "ERROR while stopping the task: \$task"
                        fi
                    done
                    
                    echo "Waiting for Service to be stable"
                    aws ecs wait services-stable \
                        --cluster ${appName}-${env.TERRAFORM_ENVIRONMENT}-ecs-cluster \
                        --services ${appName}-${env.TERRAFORM_ENVIRONMENT}-service \
                        --region eu-west-1
                """
            }
        }
    }

    post {
        always {
            script {
                if (env.BRANCH_NAME == 'master') {
                    sh "docker rmi -f ${env.image_name}:${env.image_version}-${env.image_tag} logframelab/${env.image_name}:${env.image_version}-${env.image_tag}"
                    sh "docker rmi -f ${env.image_name}:${env.image_version}-${env.image_tag} logframelab/${env.image_name}:prodlatest"
                    sh "docker rmi -f ${env.image_name}:${env.image_version}-${env.image_tag}"
                }

                else if (env.BRANCH_NAME == 'develop') {
                    sh "docker rmi -f ${env.image_name}:${env.image_version}-${env.image_tag} logframelab/${env.image_name}:${env.image_version}-${env.image_tag}"
                    sh "docker rmi -f ${env.image_name}:${env.image_version}-${env.image_tag} logframelab/${env.image_name}:devlatest"
                    sh "docker rmi -f ${env.image_name}:${env.image_version}-${env.image_tag}"
                }

                else if (KEEP_IMAGE == true) {
                    sh "docker rmi -f ${env.image_name}:${env.image_version}-${env.image_tag}"
                    sh "docker rmi -f logframelab/${env.image_name}:${env.image_version}-${env.image_tag}"
                }

            }
            cleanWs()
        }
    }
}