pipeline {
  agent any

  options {
    timestamps()
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Test') {
      steps {
        sh '''
          set -euxo pipefail
          chmod +x gradlew
          ./gradlew --no-daemon clean test
        '''
      }
    }

    stage('JaCoCo Report') {
      steps {
        sh '''
          set -euxo pipefail
          ./gradlew --no-daemon jacocoTestReport
        '''
      }
    }

    stage('Publish (MyMavenRepo)') {
      steps {
        sh '''
          set -euxo pipefail
          ./gradlew --no-daemon publish
        '''
      }
    }
  }

  post {
    always {
      // Unit test results
      junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'

      // Artifacts
      archiveArtifacts artifacts: '**/build/libs/*.jar', allowEmptyArchive: true

      // JaCoCo HTML report
      archiveArtifacts artifacts: '**/build/reports/jacoco/**', allowEmptyArchive: true

      // Javadoc (if generated)
      archiveArtifacts artifacts: '**/build/docs/javadoc/**', allowEmptyArchive: true

      // Any extra reports folder in repo
      archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
    }
  }
}
