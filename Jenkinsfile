pipeline {
  agent any

  environment {
    JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
    PATH = "${JAVA_HOME}/bin:${env.PATH}"
  }

  stages {
    stage('Check Java') {
      steps {
        sh 'echo JAVA_HOME=$JAVA_HOME'
        sh '$JAVA_HOME/bin/java -version'
        sh '$JAVA_HOME/bin/javac -version'
      }
    }

    stage('Compile') {
      steps {
        sh 'mvn clean compile'
      }
    }

    stage('Run Unit Tests') {
      steps {
        script {
          try {
            sh 'mvn test'
          } catch (Exception e) {
            echo "Tests failed: ${e.message}"
          }
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withCredentials([string(credentialsId: 'sonar-token-id', variable: 'SONAR_TOKEN')]) {
          sh "mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN -Dmaven.test.skip=true"
        }
      }
    }
    stage('OWASP Dependency Check') {
  steps {
  sh 'mvn org.owasp:dependency-check-maven:check -Danalyzer.jar.enabled=false -Danalyzer.assembly.enabled=false'
   }
}
stage('Docker Image Scan') {
  steps {
    sh '''
      for image in $(docker images --format "{{.Repository}}:{{.Tag}}"); do
        if [ "$image" != "hello-world:latest" ]; then
          echo "⏳ Scanning $image ..."
          docker run --rm aquasec/trivy image --timeout 15m "$image"
        else
          echo "⏭️ Skipping $image"
        fi
      done
    '''
  }
}
    stage('Secrets Scan') {
  steps {
    sh 'gitleaks detect --source . --exit-code 1'
  }
}
  }
   /* stage('Générer rapports') {
      steps {
        sh 'gitleaks detect --source . --report-path gitleaks-report.json'
        sh 'mvn org.owasp:dependency-check-maven:check'
        archiveArtifacts artifacts: 'gitleaks-report.json, target/dependency-check-report.html', allowEmptyArchive: true
      }
    }
  }*//*aaaabbbbbcccccdddddeeee*/
  post {
    always {
            sh 'gitleaks detect --source . --report-path gitleaks-report.json'
        sh 'mvn org.owasp:dependency-check-maven:check'
       archiveArtifacts artifacts: 'gitleaks-report.json, target/dependency-check-report.html', allowEmptyArchive: true
    }
    success {
      mail(
        to: 'medbouza200@gmail.com',
        subject: "Build Succeeded: ${env.JOB_NAME}",
        body: "Success: ${env.BUILD_URL}"
      )
    }
    failure {
      mail(
        to: 'medbouza200@gmail.com',
        subject: "Build Failed: ${env.JOB_NAME}",
        body: "See details: ${env.BUILD_URL}"
      )
    }

    // Uncomment to use MVN Nexus stage
    /*
    stage('MVN Nexus') {
      steps {
        sh 'mvn deploy -Dmaven.test.skip=true'
      }
    }
    */

/*stage('Docker Image Stage') {
  steps {
    withCredentials([usernamePassword(credentialsId: 'docker-hub-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
      sh """
        docker login -u $DOCKER_USER -p $DOCKER_PASS
        docker pull yourusername/myproject:latest
        docker tag yourusername/myproject:latest yourusername/myproject:new-tag
        docker push yourusername/myproject:new-tag
      """
    }
  }
}
*/
    /*stage('Run Docker Compose') {
      steps {
        sh '''
          echo "🔧 Démarrage des services avec Docker Compose..."

          # Vérifie quelle commande est disponible
          if command -v docker compose > /dev/null; then
            echo "✅ Utilisation de 'docker compose'"
            docker compose up -d
            docker compose ps
          elif command -v docker-compose > /dev/null; then
            echo "✅ Utilisation de 'docker-compose'"
            docker-compose up -d
            docker-compose ps
          else
            echo "❌ Ni 'docker compose' ni 'docker-compose' ne sont disponibles."
            exit 1
          fi
        '''
      }
    }*/
}
}
