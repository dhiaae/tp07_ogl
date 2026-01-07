pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean test'
            }
        }
    }

    post {
        always {
            // JUnit reports (si générés par Gradle)
            junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'

            // Cucumber JSON report (ton repo a reports/example-report.json)
            cucumber buildStatus: 'UNSTABLE', fileIncludePattern: '**/reports/*.json'

            // Archiver les rapports HTML si tu les veux dans Jenkins
            archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
        }
    }
}
