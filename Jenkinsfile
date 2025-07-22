pipeline {
    agent any // Soh possuo um agent

    stages {
        stage('Checkout') {
            steps {
                // Busca o codigo do repositorio
                checkout scm
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    // Define um nome e tag para a imagem
                    def imageName = "welington/sc-mensageria:latest"

                    // Usa o plugin Docker para construir a imagem do Dockerfile no repositorio
                    // O '.' significa "use o diretorio corrente como o contexto de construcao
                    docker.build(imageName, '.')
                }
            }
        }
         // stage('Test') { ... }
        // stage('Push to Registry') { ... }
        // stage('Deploy') { ... }
    }
}