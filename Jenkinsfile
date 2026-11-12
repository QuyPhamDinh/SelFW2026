pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
        skipDefaultCheckout(true)
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        TEST_ENV = 'ci'
        SELENIUM_REMOTE_URL = 'http://localhost:4444'
        TF_IN_AUTOMATION = 'true'
        TF_INPUT = 'false'
        SELENIUM_PORT = '4444'
    }

    stages {
        stage('Checkout Source') {
            steps {
                checkout scm
            }
        }

        stage('Verify Prerequisites') {
            steps {
                sh '''
                    set -eu
                    java -version
                    mvn -version
                    docker version
                    terraform version
                '''
            }
        }

        stage('Validate Terraform') {
            steps {
                sh '''
                    set -eu
                    terraform -chdir=terraform init -backend=false -input=false
                    terraform -chdir=terraform fmt -check
                    terraform -chdir=terraform validate
                '''
            }
        }

        stage('Provision Infrastructure') {
            steps {
                script {
                    env.TF_CONTAINER_NAME = "selfw-selenium-${env.BUILD_NUMBER}"
                }
                sh '''
                    set -eu
                    terraform -chdir=terraform apply -auto-approve -input=false \
                      -var="container_name=${TF_CONTAINER_NAME}" \
                      -var="host_port=${SELENIUM_PORT}"
                '''
            }
        }

        stage('Wait for Selenium Grid') {
            steps {
                sh '''
                    set -eu
                    for attempt in $(seq 1 60); do
                      if curl --fail --silent "${SELENIUM_REMOTE_URL}/status" | grep -Eq '"ready"[[:space:]]*:[[:space:]]*true'; then
                        exit 0
                      fi
                      sleep 2
                    done
                    echo "Selenium Grid did not become ready in time" >&2
                    exit 1
                '''
            }
        }

        stage('Execute Smoke Tests') {
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh 'mvn -B test -Denv=ci -Dheadless=true -Dcucumber.filter.tags="@smoke"'
                }
            }
        }

        stage('Execute API Tests') {
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh 'mvn -B test -Denv=ci -Dcucumber.filter.tags="@api"'
                }
            }
        }

        stage('Execute UI Regression Tests') {
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh 'mvn -B test -Denv=ci -Dheadless=true -Dcucumber.filter.tags="@ui"'
                }
            }
        }

        stage('Publish Reports') {
            steps {
                junit allowEmptyResults: true, testResults: 'target/surefire-reports/**/*.xml'
                archiveArtifacts allowEmptyArchive: true, artifacts: 'target/cucumber-report.*,target/screenshots/**/*,allure-results/**/*,target/allure-results/**/*', fingerprint: true
                script {
                    if (fileExists('allure-results')) {
                        allure includeProperties: false, jdk: '', results: [[path: 'allure-results']]
                    } else if (fileExists('target/allure-results')) {
                        allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
                    }
                }
            }
        }
    }

    post {
        always {
            sh '''
                if [ -f terraform/terraform.tfstate ]; then
                  terraform -chdir=terraform destroy -auto-approve -input=false \
                    -var="container_name=${TF_CONTAINER_NAME:-selfw-selenium-${BUILD_NUMBER}}" \
                    -var="host_port=${SELENIUM_PORT}"
                fi
            '''
        }
    }
}
