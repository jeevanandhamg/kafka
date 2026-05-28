pipeline {
    agent any

    environment {
        // credentials stored in Jenkins — not hardcoded
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-creds')
        IMAGE_NAME = 'jeeva97/kafka-springboot-app'
    }

    stages {

        stage('Checkout') {
            steps {
                // pulls your code from GitHub
                git branch: 'main',
                    url: 'https://github.com/jeevanandhamg/kafka.git'
            }
        }

        stage('Build') {
            steps {
                // compiles and packages your Spring Boot app
                sh 'mvn clean package -DskipTests'
            }
        }

//         stage('Test') {
//             steps {
//                 // runs your unit tests
//                 sh 'mvn test'
//             }
//             post {
//                 always {
//                     // publish test results in Jenkins UI
//                     junit 'target/surefire-reports/*.xml'
//                 }
//             }
//         }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
                sh "docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:latest"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh "echo ${DOCKER_HUB_CREDENTIALS_PSW} | docker login -u ${DOCKER_HUB_CREDENTIALS_USR} --password-stdin"
                sh "docker push ${IMAGE_NAME}:${BUILD_NUMBER}"
                sh "docker push ${IMAGE_NAME}:latest"
            }
        }

//         stage('Deploy to Kubernetes') {
//             steps {
//                 sh "kubectl set image deployment/springboot-kafka-app springboot-kafka-app=${IMAGE_NAME}:${BUILD_NUMBER}"
//                 sh "kubectl rollout status deployment/springboot-kafka-app"
//             }
//         }
    }

    post {
        success {
            echo 'Pipeline succeeded! App deployed successfully.'
        }
        failure {
            echo 'Pipeline failed! Check the logs.'
        }
    }
}