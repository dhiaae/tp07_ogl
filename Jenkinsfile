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
                echo 'Lancement des tests + génération du rapport JaCoCo...'
                retry(2) {
                    // IMPORTANT: jacocoTestReport generates the XML Sonar needs
                    sh './gradlew test jacocoTestReport --no-daemon --refresh-dependencies'
                }

                // JUnit test reports
                junit 'build/test-results/test/*.xml'

                // Archive JaCoCo HTML + XML (optional but useful)
                archiveArtifacts artifacts: 'build/reports/jacoco/test/**/*', fingerprint: true, allowEmptyArchive: true
            }
        }

        stage('Code Analysis (SonarQube)') {
            steps {
                echo 'Analyse du code avec SonarQube...'
                script {
                    try {
                        withSonarQubeEnv('SonarQube') {
                            // Most common task name with org.sonarqube plugin:
                            // - older: sonarqube
                            // - newer: sonar
                            // We'll try sonar first, and fallback to sonarqube.
                            sh './gradlew sonar --no-daemon || ./gradlew sonarqube --no-daemon'
                        }
                    } catch (Exception e) {
                        echo "SonarQube analysis failed: ${e.message}"
                        // If you want pipeline to fail when Sonar fails, uncomment next line:
                        // error("Stopping pipeline because SonarQube analysis failed")
                    }
                }
            }
        }

        stage('Code Quality (Quality Gate)') {
            steps {
                echo 'Vérification des Quality Gates...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Construction du projet...'
                sh './gradlew build -x test --no-daemon'
                sh './gradlew javadoc --no-daemon'

                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true, allowEmptyArchive: true
                archiveArtifacts artifacts: 'build/docs/**/*', fingerprint: true, allowEmptyArchive: true

                echo 'Build terminé'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Déploiement...'
                script {
                    try {
                        sh './gradlew publish --no-daemon'
                        echo 'Déploiement réussi'
                    } catch (Exception e) {
                        echo "Deploy failed: ${e.message}"
                        // If you want pipeline to fail on deploy failure:
                        // error("Stopping pipeline because deploy failed")
                    }
                }
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
