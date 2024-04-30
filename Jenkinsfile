pipeline {
    agent { docker { image 'jelastic/maven:3.9.5-openjdk-21' } }
    stages {
        stage("Maven container start"){
            steps{
            sh 'docker compose --profile maven up -d'
            }
        }
        stage('build') {
            steps {
                sh 'mvn --version'
            }
        }
    }
}