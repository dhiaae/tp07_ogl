pipeline {
  agent any

  options {
    timestamps()
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Simple build') {
      steps {
        sh '''
          set -euxo pipefail
          chmod +x gradlew
          ./gradlew --no-daemon clean build
        '''
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: '**/build/libs/*.jar', allowEmptyArchive: true
      junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
    }
  }
}
