pipeline {
    agent any

    tools {
            maven 'maven-3'
            //dockerTool 'default' // This activates the Docker CLI for your steps
        }
    environment {
        // credentials stored in Jenkins — not hardcoded
        //DOCKER_HUB_CREDENTIALS = credentials('docker-hub-creds')
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

//         stage('Build Docker Image') {
//             steps {
//                 sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
//                 sh "docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:latest"
//             }
//         }

            stage('Build Docker Image') {
                steps {
                    script {
                    // Downloads a modern, static Linux Docker CLI binary if it doesn't exist
                    sh '''
                    if ! command -v docker &> /dev/null; then
                        echo "Docker CLI not found. Installing modern static binary..."
                        curl -fsSL https://download.docker.com/linux/static/stable/aarch64/docker-26.1.3.tgz -o docker.tgz
                        tar -xzvf docker.tgz
                        mv docker/docker /usr/local/bin/
                        rm -rf docker docker.tgz
                    fi
                    '''

                    // Now your build commands will run flawlessly using the modern client
                    sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
                    sh "docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:latest"
                }
            }
        }

        stage('Push to Docker Hub') {
                    steps {
                        // Keep these as generic variable names inside single quotes!
                        // Jenkins will inject your real credentials into them securely.
                        withCredentials([usernamePassword(credentialsId: 'docker-hub-creds',
                                                         usernameVariable: 'DOCKER_USER',
                                                         passwordVariable: 'DOCKER_PASS')]) {

                            // Using backslashes \$ ensures shell interpolation rather than Groovy leakage
                            sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin"
                            sh "docker push ${IMAGE_NAME}:${BUILD_NUMBER}"
                            sh "docker push ${IMAGE_NAME}:latest"
                        }
                    }
                    post {
                        always {
                            sh "docker logout"
                        }
                    }
                }

//         stage('Deploy to Kubernetes') {
//             steps {
//                 sh "kubectl set image deployment/springboot-kafka-app springboot-kafka-app=${IMAGE_NAME}:${BUILD_NUMBER}"
//                 sh "kubectl rollout status deployment/springboot-kafka-app"
//             }
//         }

stage('Deploy to Kubernetes') {
            agent {
                docker {
                    image 'bitnami/kubectl:latest'
                    // 🚀 ADDED: --network=host lets the container see your Mac's localhost cluster
                    args '-u root --network=host --entrypoint="" -v /var/jenkins_home/.kube:/config/.kube:ro'
                }
            }
            steps {
                script {
                    withEnv(["KUBECONFIG=/config/.kube/config"]) {
                        sh "kubectl apply -f k8s-deployment.yaml"
                        sh "kubectl rollout restart deployment/kafka-springboot-app-deployment -n dev"
                        sh "kubectl rollout status deployment/kafka-springboot-app-deployment -n dev"
                    }
                }
            }
        }
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

