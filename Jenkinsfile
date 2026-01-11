pipeline {
    agent any

    environment {
        PROJECT_NAME = 'TP7-API'
    }

    stages {

        stage('Debug Agent') {
            steps {
                sh 'uname -a'
                sh 'java -version'
                sh 'chmod +x gradlew || true'
                sh './gradlew --version'
            }
        }

        stage('Clean') {
            steps {
                echo 'Nettoyage...'
                sh './gradlew clean --no-daemon --refresh-dependencies'
            }
        }

        stage('Test + Coverage (JaCoCo)') {
            steps {
                echo 'Tests + JaCoCo...'
                retry(2) {
                    sh './gradlew test jacocoTestReport --no-daemon --refresh-dependencies'
                }




                junit 'build/test-results/test/*.xml'
                archiveArtifacts artifacts: 'build/reports/jacoco/test/**/*', fingerprint: true, allowEmptyArchive: true
            }
        }
        stage('cucumber') {
            steps {
                echo 'Tests + JaCoCo...'
                 cucumber buildStatus: 'UNSTABLE',
                 reportTitle: 'My report',
                 fileIncludePattern: 'reports/*.json',
                 trendsLimit: 10

                }




                junit 'build/test-results/test/*.xml'
                archiveArtifacts artifacts: 'build/reports/jacoco/test/**/*', fingerprint: true, allowEmptyArchive: true
            }
        }
        stage('Code Analysis (SonarQube)') {
            steps {
                echo 'Analyse SonarQube...'

                // IMPORTANT: No try/catch here. If Sonar fails, pipeline should fail.
                withSonarQubeEnv('sonar') {
                    // Run sonar (preferred) or sonarqube (fallback).
                    // If BOTH fail, exit non-zero so Jenkins stops.
                    sh '''
                        set -e
                        ./gradlew sonar --no-daemon || ./gradlew sonarqube --no-daemon
                    '''
                }
            }
        }

        stage('Code Quality (Quality Gate)') {
            steps {
                echo 'Vérification Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Build...'
                sh './gradlew build -x test --no-daemon'
                sh './gradlew javadoc --no-daemon'

                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true, allowEmptyArchive: true
                archiveArtifacts artifacts: 'build/docs/**/*', fingerprint: true, allowEmptyArchive: true
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploy...'
                sh './gradlew publish --no-daemon'
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline réussi !'

            slackSend(
                channel: '#general',
                color: 'good',
                message: "✅ Déploiement réussi !\nProjet: ${env.JOB_NAME}\nBuild: #${env.BUILD_NUMBER}\nDate: ${new Date().format('yyyy-MM-dd HH:mm')}"
            )

            emailext (
                subject: "✅ Build Réussi - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    <h2>✅ Build Réussi</h2>
                    <p><b>Projet :</b> ${env.JOB_NAME}</p>
                    <p><b>Build n° :</b> ${env.BUILD_NUMBER}</p>
                    <p><b>Date :</b> ${new Date()}</p>
                    <p><a href="${env.BUILD_URL}">Voir les détails du build</a></p>
                """,
                to: 'md_harti@esi.dz',
                mimeType: 'text/html'
            )
        }

        failure {
            echo '❌ Pipeline échoué !'

            slackSend(
                channel: '#general',
                color: 'danger',
                message: "❌ Échec du build !\nProjet: ${env.JOB_NAME}\nBuild: #${env.BUILD_NUMBER}\nLogs: ${env.BUILD_URL}"
            )

            emailext (
                subject: "❌ Build Échoué - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    <h2>❌ Build Échoué</h2>
                    <p><b>Projet :</b> ${env.JOB_NAME}</p>
                    <p><b>Build n° :</b> ${env.BUILD_NUMBER}</p>
                    <p><b>Erreur :</b> Une ou plusieurs étapes ont échoué.</p>
                    <p><a href="${env.BUILD_URL}console">Voir les logs complets</a></p>
                """,
                to: 'md_harti@esi.dz',
                mimeType: 'text/html'
            )
        }
    }
}
