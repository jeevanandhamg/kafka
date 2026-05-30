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
                    args '-u root --entrypoint=""'
                }
            }
            steps {
                script {
                    // Create an isolated workspace directory for certificates and config
                    sh "mkdir -p .kube-temp"

                    // 1. Write the raw base64 strings securely into files within the runner
                    sh "echo 'LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURRakNDQWlxZ0F3SUJBZ0lJRk1tVGk5emdBcUF3RFFZSktvWklodmNOQVFFTEJRQXdGVEVUTUJFR0ExVUUKQXhNS2EzVmlaWEp1WlhSbGN6QWVGdzB5TmpBMU16QXhNREUyTkRKYUZ3MHlOekExTXpBeE1ERTJOREphTURZeApGekFWQmdOVkJBb1REbk41YzNSbGJUcHRZWE4wWlhKek1Sc3dHUVlEVlFRREV4SmtiMk5yWlhJdFptOXlMV1JsCmMydDBiM0F3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRRHNRQVF2dlhrRFpBZDgKbWhrbEN4T01MN3NRS1F2enZnaXZ2TmdkWDJ0MGRZOURDdGU2VEd2NG5XdG5jL1phczc0NWxPM0J5V0tsS3VneApSblRmbTA3Y0VETUtwWWZQL2FRTTIvRlFrbjhlaVZvNmo3MDBjbXVOckJlTzFJSVl0K043cjBxZnY3Z2VEMVdECnNIMWM4L3FQWHpZUnVDZFh6WTNsamtzN0RXSW5sY1Jua1ozaHVlTDd5UWg0elBBZXZ4N09yYWs3Z0R0UmxXb28KTzloci9TejMyY1drK0RRQ09kZkFlUUc0SjJoWEgrWUlDUk04bXc5NERpVkw0dE02U2FTMUJkSHdmWmhEVEJRSAo2RUhKbm8rZEtKdHprZFJqNUd5TjRXZzRQOW0vbXNQY3I2TUlMNlNQTU1XNHdlUkoxZ3UwZTZTdXk4T21oS3NtCm05dGpGMG5WQWdNQkFBR2pkVEJ6TUE0R0ExVWREd0VCL3dRRUF3SUZvREFUQmdOVkhTVUVEREFLQmdnckJnRUYKQlFjREFqQU1CZ05WSFJNQkFmOEVBakFBTUI4R0ExVWRJd1FZTUJhQUZEak1UZ2tDcEVpWEFQR0RxT09HUWhsdQpTZ0p0TUIwR0ExVWRFUVFXTUJTQ0VtUnZZMnRsY2kxbWIzSXRaR1Z6YTNSdmNEQU5EZ2txaGtpRzl3MEJBUXNGCkFBT0NBUUVBcHBoQzNqNTAxeC8zblJTaHllY1BxNWgvbGd5NWMrR05UU0l6bGZwNmkwaGhvZlVKNlZLMUZKbEcKUnQ5Qk55VE1jVnlUdE9sS3g3RjRGeE5JQkhjMUpsR09EcUZRSDJGcUExT25xNEpxSWw2clVsTzBua0lHYnozMApxWHhnNG9YSEpNb1I5YWJSbXZDaDhGOFNKMTd3NDk1K1MrYjlQUCtTTlRQeUZOdG9WeVRnQmFpeThLZk1GenozClB4NCtFT1g4YlhkYngrb1ZqTWZlUW9NQy9tRWRuWm4xSmVKM2UzVEh6aG1zc2pldkk0Z0pWUU1RdUtYM0VpNjgKUXVGdXhIWXBMbFRJbng5ZGxpQmxUQWhmWFg3ZXNETkl0VHhBZldMemZWc2JSa1NNc05MamNVQ2RGZ0xIWVprdwpaRWw3N28yUFUwMDZGOVpvRldoUFFmTDBvTlJXd0E9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0t' | base64 -d > .kube-temp/client.crt"

                    sh "echo 'LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcFFJQkFBS0NBUUVBN0VBRUw3MTVBMlFIZkpvWkpRc1RqQys3RUNrTDg3NElyN3pZSFY5cmRIV1BRd3JYCnVreHIrSjFyWjNQMldyTytPWlR0d2NsaXBTcm9NVVowMzV0TzNCQXpDcVdIei8ya0ROdnhVSkovSG9sYU9vKzkKTkhKcmphd1hqdFNDR0xmamU2OUtuNys0SGc5Vmc3QjlYUFA2ajE4MkViZ25WODJONVk1TE93MWlKNVhFWjVHZAo0Ym5pKzhrSWVNendIcjhlenEycE80QTdVWlZxS0R2WWEvMHM5OW5GcFBnMEFqblh3SGtCdUNkb1Z4L21DQWtUClBKc1BlQTRsUytMVE9rbWt0UVhSOEgyWVEwd1VCK2hCeVo2UG5TaWJjNUhVWStSc2plRm9PRC9adjVyRDNLK2oKQ0Mra2p6REZ1TUhrU2RZTHRIdWtyc3ZEcG9TckpwdmJZeGRKMVFJREFRQUJBb0lCQUIvRzkra3dwcWpaUTcvZQpVdzNaQUxtYXAvZU9BSVlQWkhLNFIzek5SaEphOG1OVnFqRVhCaG8yN21pZGFxQnc5elZ6NktFU3hLVXJ4c3lZCm4xclR0SmFaZFRiSmozYmZYK1FTUE5JV0g3N01seW1KQmxoRXl3a1BnZmNHY3QzK24yZHJ3TWF6dVN1NGxmWTAKZjBCSldacXBvZ2V6SDFPZm1mdXJ3L0crK2U5bk9GOTZxSnVBenpIZHRiQ3FWWW1teG9CZUJKdHdkbTVTUDUvVQplNkZ4SzhCSGtPOVQvcEkxemVNUFNYKy9rYjZXN01zNXErYVErREhJV2dYdmFFVXI4aGx5TTRYaWRjOHdJWk5XClM5b3FxLzZnbHRETk9UaktLcUtueVRJcEYyN2p3MG5XdThQa0JhcUNQQ28rODh1eC9GYkg4NkFCUXdJbjl6MDEKM3lLdWRWY0NnWUVBOEJVTlQ5RHBLbjMrTThKOVdJL0N4SlBzdkJDeldtMC8raWMvTjlZeTRZdndLRGNGeFFHUgpVUTJDbU1pNDc5RFc5SzloZEFpU0V2MHV3MkxuRU9VcVNYK3FQaVNRZkpVelJlR3hlSGJtYmR6Tm8zSEloNjgvCjV5MXMrTWNhK1FxbkNaNzFPTDI5UEUxVTRVTXNTSDVQc2s2bVd3c1hTcW96ay9yclk4dkx0M2NDZ1lFQSsrbnIKbi8xMlJhSndsdk5pRkk5OElMYjhLUzB3ZHA1TDUyMWg4emZkQXJ2VUlOQTZhTklUVnlpR1NVb1YzZ3E4UUx3YwpiT2NGdzh0cFJpcnhMQzhqOU10TkxJalQ2aFZxazZER1pjdmN6UWtOUnNZWGVoaWF3cmRiWnMzS1lqd0ZMa1FkCjFXUFBnclpzRWdVbmR3OFRTZENBWC9wZHZkOTNKWkhmQjJvUHRCTUNnWUVBazl5elhTUVNacnhBVWtxSGZvTFQKOXRRUUttZXl2bytvcG4yRGZ2VXFVeVVubk96K1hhNHdmSlh3aC82ZmYvdkVWK0NvQ1ptNXRYNC9UZERjOU5mZgpLbk01TzVxemJqZEo0OUV6eHppYmhMQkg2bEVLcXg1eElnVWxKemNoVXA2UDcrbjVwNStjSzhYSTcwKzZ0MDhyCjhHcG1KSTU1SXVBZWNoWkk0U2JENmRNQ2dZRUFyQ2xvMjlXSUgvenMvMnprS2ZNSnZQM3hoVjZaMFdkTHJxVWsKN2ZQT3VwQ05YOE9QTjVYaW05MVpNUHROeUlzRHJ5WWdNeGtMNm95NHJMaXFUQzBIWU1RVVFReGQ3NHVsWTdFcwpCZVAyU2JZVytiaGwwUTdCcmJOTDV3MWJkQmxhM1F5MmF1Q2tyOHRtUGthQmV5KzFXZXdCNEJZbVBKNWRPakxmCi9wd3drVHNDZ1lFQTR2Q3FHYXJjK25odS9FczA3UHFrSmdzb0dFdmVrNEtkWCtNRWtnS2JkZFk1bVR2UENKRlAKWUxLSmdaVU9kNHFTQnVhaS95UkpXaHN5QkVZSCtTV0JRMmE0dzV0TWluNXk5NkdqS3BSaWFrSUR5ZXIxR2V3egphS0Y4K0dmMDlyNUVSOVlTS0ZMZ1d6aWk4dUFLQzVoaThjMWc4cDlnREJkdzNTc2EvaXVXaExZPQotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo=' | base64 -d > .kube-temp/client.key"

                    withEnv(["KUBECONFIG=${WORKSPACE}/.kube-temp/config"]) {
                        // 2. Point target cluster to the Docker local network bridge domain
                        sh "kubectl config set-cluster local-cluster --server=https://kubernetes.docker.internal:6443 --insecure-skip-tls-verify=true"

                        // 3. Register credentials via the extracted files safely
                        sh "kubectl config set-credentials pipeline-user --client-certificate=.kube-temp/client.crt --client-key=.kube-temp/client.key"

                        // 4. Synthesize the operational deployment execution context
                        sh "kubectl config set-context pipeline-ctx --cluster=local-cluster --user=pipeline-user"
                        sh "kubectl config use-context pipeline-ctx"

                        // 5. Fire off updates cleanly
                        sh "kubectl apply -f k8s-deployment.yaml --validate=false"
                        sh "kubectl rollout restart deployment/kafka-springboot-app-deployment -n dev"
                        sh "kubectl rollout status deployment/kafka-springboot-app-deployment -n dev"
                    }
                }
            }
            post {
                always {
                    // Tear down our configuration directory out of memory completely
                    sh "rm -rf .kube-temp"
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

