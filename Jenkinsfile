pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Test') {
      steps {
        sh '''
          set -euxo pipefail
          chmod +x gradlew
          ./gradlew --no-daemon clean test jacocoTestReport
        '''
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
          cucumber buildStatus: 'UNSTABLE', fileIncludePattern: '**/reports/*.json, **/build/**/*.json'
          archiveArtifacts artifacts: 'reports/**, **/build/reports/**', allowEmptyArchive: true
        }
      }
    }

    stage('Code Analysis (SonarQube)') {
      steps {
        withSonarQubeEnv('Sonar') {
          sh './gradlew --no-daemon sonar'
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 5, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Build') {
      steps {
        sh '''
          set -euxo pipefail
          ./gradlew --no-daemon jar javadoc
        '''
      }
      post {
        always {
          archiveArtifacts artifacts: '**/build/libs/*.jar, **/build/docs/javadoc/**', allowEmptyArchive: true
        }
      }
    }

    stage('Deploy (MyMavenRepo)') {
      steps {
        sh '''
          set -euxo pipefail
          ./gradlew --no-daemon publish
        '''
      }
    }
  }

  post {
    success {
      sh '''
        set -e
        if [ -n "${SLACK_WEBHOOK:-}" ]; then
          curl -sS -X POST -H 'Content-Type: application/json' \
            --data '{"text":"✅ Déploiement réussi (Jenkins: '"${JOB_NAME}"' #'"${BUILD_NUMBER}"')"}' \
            "$SLACK_WEBHOOK" >/dev/null
        fi
      '''

      // Requires EMAIL_TO set in Jenkins Global properties
      mail to: "${EMAIL_TO}",
           subject: "✅ Déploiement réussi - ${JOB_NAME} #${BUILD_NUMBER}",
           body: "Déploiement réussi.\nJob: ${JOB_NAME}\nBuild: #${BUILD_NUMBER}\nURL: ${BUILD_URL}\n"
    }

    failure {
      sh '''
        set -e
        if [ -n "${SLACK_WEBHOOK:-}" ]; then
          curl -sS -X POST -H 'Content-Type: application/json' \
            --data '{"text":"❌ Pipeline FAILED (Jenkins: '"${JOB_NAME}"' #'"${BUILD_NUMBER}"')\\nVoir: '"${BUILD_URL}"'"}' \
            "$SLACK_WEBHOOK" >/dev/null
        fi
      '''

      mail to: "${EMAIL_TO}",
           subject: "❌ Pipeline FAILED - ${JOB_NAME} #${BUILD_NUMBER}",
           body: "Pipeline FAILED.\nJob: ${JOB_NAME}\nBuild: #${BUILD_NUMBER}\nURL: ${BUILD_URL}\n"
    }

    always {
      archiveArtifacts artifacts: '**/build/reports/**, reports/**', allowEmptyArchive: true
    }
  }
}
