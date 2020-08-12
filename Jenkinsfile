pipeline {
    agent any
    tools {
        maven 'Maven'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -DskipTests clean package'
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
                    archiveArtifacts 'target/*.jar'
                }
            }
        }

        stage('Deploy Dev') {
            when {
                branch 'develop'
            }
            steps {
                sh 'docker-compose up --build -d'
            }
        }

        stage('Deploy Prod') {
            when {
                branch 'master'
            }
            steps {
                sshPublisher(publishers: [sshPublisherDesc(configName: 'PROD', transfers:[
                 sshTransfer(cleanRemote: false, excludes: '', execCommand: '', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: 'logframelab-server', remoteDirectorySDF: false, removePrefix: '', sourceFiles: 'target/*.jar'),
                 sshTransfer(cleanRemote: false, excludes: '', execCommand: '', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: 'logframelab-server', remoteDirectorySDF: false, removePrefix: '', sourceFiles: 'Dockerfile'),
                 sshTransfer(cleanRemote: false, excludes: '', execCommand: 'docker-compose up --build -d', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: 'logframelab-server', remoteDirectorySDF: false, removePrefix: '', sourceFiles: 'docker-compose.yml')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false)])  }
            }
        }
    }
}