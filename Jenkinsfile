pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    tools {
        maven 'maven-3'
    }

    environment {
        IMAGE_NAME = 'jeeva97/kafka-springboot-app'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/jeevanandhamg/kafka.git'
            }
        }

        // ✅ Add this right after Checkout
        stage('Check Commit') {
            steps {
                script {
                    def commitMsg = sh(
                        script: 'git log -1 --pretty=%B',
                        returnStdout: true
                    ).trim()

                    echo "Commit message: ${commitMsg}"

                    if (commitMsg.contains('ci: update image tag')) {
                        currentBuild.result = 'SUCCESS'
                        error('Skipping — Jenkins tag update commit. Aborting pipeline.')
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh '''
                    if ! command -v docker &> /dev/null; then
                        curl -fsSL https://download.docker.com/linux/static/stable/aarch64/docker-26.1.3.tgz -o docker.tgz
                        tar -xzvf docker.tgz
                        mv docker/docker /usr/local/bin/
                        rm -rf docker docker.tgz
                    fi
                    '''
                    sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
                    sh "docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:latest"
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds',
                                                 usernameVariable: 'DOCKER_USER',
                                                 passwordVariable: 'DOCKER_PASS')]) {
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

        stage('Update Image Tag in Git') {
            steps {
                withCredentials([string(credentialsId: 'github-token', variable: 'GIT_TOKEN')]) {
                    sh """
                        git config user.name "jeevanandhamg"
                        git config user.email "jeevanandham97gksj@gmail.com"

                        sed -i "s|jeeva97/kafka-springboot-app:.*|jeeva97/kafka-springboot-app:${BUILD_NUMBER}|g" k8s/k8s-deployment.yaml

                        git add k8s/k8s-deployment.yaml
                        git commit -m "ci: update image tag to ${BUILD_NUMBER}"
                        git push https://\${GIT_TOKEN}@github.com/jeevanandhamg/kafka.git main
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed! Check the logs.'
        }
    }
}