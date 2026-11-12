pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
        skipDefaultCheckout(true)
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        PATH = "/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/Applications/Docker.app/Contents/Resources/bin:${env.PATH}"
        COMPOSE_PROJECT_NAME = "selfw-${env.BUILD_NUMBER}"
        TEST_ENV = 'ci'
    }

    stages {
        stage('Checkout Source') {
            steps {
                checkout scm
            }
        }

        stage('Verify Docker Access') {
            steps {
                sh '''
                    set -eu
                    command -v docker
                    docker compose version
                    docker version
                    docker info >/dev/null
                '''
            }
        }

        stage('Build Test Environment') {
            steps {
                sh '''
                    set -eu
                    mkdir -p target allure-results
                    chmod -R a+rwX target allure-results
                    docker compose build test-runner
                    docker compose run --rm --no-deps --entrypoint java test-runner -version
                    docker compose run --rm --no-deps --entrypoint mvn test-runner -version
                '''
            }
        }

        stage('Start Selenium Grid') {
            steps {
                sh 'docker compose up -d --wait selenium'
            }
        }

        stage('Execute Maven Tests') {
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh 'docker compose run --rm --no-deps test-runner'
                }
            }
        }

        stage('Collect Reports') {
            steps {
                sh '''
                    set +e
                    docker compose logs selenium > target/selenium.log 2>&1
                    exit 0
                '''
            }
        }

        stage('Publish Reports') {
            steps {
                junit allowEmptyResults: true, testResults: 'target/surefire-reports/**/*.xml'
                archiveArtifacts allowEmptyArchive: true,
                    artifacts: 'target/cucumber-report.*,target/screenshots/**/*,target/selenium.log,allure-results/**/*,target/allure-results/**/*',
                    fingerprint: true
            }
        }
    }

    post {
        always {
            sh 'docker compose down --remove-orphans'
        }
    }
}
