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

    /* stage('MVN Nexus') {
      steps {
        sh 'mvn deploy -Dmaven.test.skip=true'
      }
    } */

    stage('Docker Image Stage') {
      steps {
        sh """
          # Login to Docker
          docker login -u anisbm3 -p 25/01/2003
          
          # Pull image to make sure it's available locally
          docker pull anisbm3/anisbenmehrez-4twin2-g4-stationski:1.0.0
          
          # Tag the pulled image with a new tag
          docker tag anisbm3/anisbenmehrez-4twin2-g4-stationski:1.0.0 anisbm3/anisbenmehrez-4twin2-g4-stationski:new-tag
          
          # Push the image with the new tag
          docker push anisbm3/anisbenmehrez-4twin2-g4-stationski:new-tag
        """
      }
    }
stage('Run Docker Compose') {
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
}

  }
}
