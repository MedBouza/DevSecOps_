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
