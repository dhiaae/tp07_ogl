pipeline {
  agent any

  options {
    timestamps()
    skipDefaultCheckout(true)
  }

  environment {
    // Slack webhook must be set in Jenkins Global properties as SLACK_WEBHOOK
    // Email uses Jenkins SMTP config; "mail" step uses that.
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    // 2.1 Test
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
          // 1) Archive unit test results
          junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'

          // 2) Publish cucumber reports (expects JSON file(s))
          // If your project produces cucumber json under reports/, keep this:
          cucumber buildStatus: 'UNSTABLE', fileIncludePattern: '**/reports/*.json, **/build/**/*.json'

          // Keep any reports for proof
          archiveArtifacts artifacts: 'reports/**, **/build/reports/**', allowEmptyArchive: true
        }
      }
    }

    // 2.2 Code Analysis
    stage('Code Analysis (SonarQube)') {
      steps {
        withSonarQubeEnv('Sonar') {
          sh './gradlew --no-daemon sonar'
        }
      }
    }

    // 2.3 Code Quality
    stage('Quality Gate') {
      steps {
        timeout(time: 5, unit: 'MINUTES') {
          // Will abort pipeline if gate is FAILED
          waitForQualityGate abortPipeline: true
        }
      }
    }

    // 2.4 Build
    stage('Build') {
      steps {
        sh '''
          set -euxo pipefail
          ./gradlew --no-daemon jar javadoc
        '''
      }
      post {
        always {
          // Archive jar + documentation
          archiveArtifacts artifacts: '**/build/libs/*.jar, **/build/docs/javadoc/**', allowEmptyArchive: true
        }
      }
    }

    // 2.5 Deploy
    stage('Deploy (MyMavenRepo)') {
      steps {
        sh '''
          set -euxo pipefail
          ./gradlew --no-daemon publish
        '''
      }
    }
  }

  // 2.6 Notification (success + failure)
  post {
    success {
      // Slack success
      sh '''
        set -e
        if [ -n "${SLACK_WEBHOOK:-}" ]; then
          curl -sS -X POST -H 'Content-Type: application/json' \
            --data '{"text":"✅ Déploiement réussi (Jenkins: '"${JOB_NAME}"' #'"${BUILD_NUMBER}"')"}' \
            "$SLACK_WEBHOOK" >/dev/null
        fi
      '''
      // Email success (uses Jenkins SMTP config)
      mail to: "${EMAIL_TO}",
           subject: "✅ Déploiement réussi - ${JOB_NAME} #${BUILD_NUMBER}",
           body: "Déploiement réussi.\nJob: ${JOB_NAME}\nBuild: #${BUILD_NUMBER}\nURL: ${BUILD_URL}\n"
    }

    failure {
      // Slack failure
      sh '''
        set -e
        if [ -n "${SLACK_WEBHOOK:-}" ]; then
          curl -sS -X POST -H 'Content-Type: application/json' \
            --data '{"text":"❌ Pipeline FAILED (Jenkins: '"${JOB_NAME}"' #'"${BUILD_NUMBER}"')\\nVoir: '"${BUILD_URL}"'"}' \
            "$SLACK_WEBHOOK" >/dev/null
        fi
      '''
      // Email failure
      mail to: "${EMAIL_TO}",
           subject: "❌ Pipeline FAILED - ${JOB_NAME} #${BUILD_NUMBER}",
           body: "Pipeline FAILED.\nJob: ${JOB_NAME}\nBuild: #${BUILD_NUMBER}\nURL: ${BUILD_URL}\n"
    }

    always {
      // Keep logs/artifacts accessible
      archiveArtifacts artifacts: '**/build/reports/**, reports/**', allowEmptyArchive: true
    }
  }
}
