pipeline {
    agent any // Soh possuo um agent
    options {
            // Previne checkout default no inicio do pipeline
            skipDefaultCheckout()
        }
    stages {
        stage('Checkout') {
            steps {
                // Busca o codigo do repositorio
                checkout scm
            }
        }
  // --- STAGE 1: Teste (Fail-Fast) ---
        // Rodar testes antes de mais nada. Se falhar, o pipeline para.
        stage('Test') {
            steps {
                // Garante que o script do Maven Wrapper tenha permissão de execução
                // no workspace do Jenkins antes de ser chamado.
                sh 'chmod +x mvnw'
                // a variavel de ambiente que informa ao Springboot onde encontrar o RabbitMQ
                //dentro da rede do docker compose. 'rabbitmq' é o nome do servico no docker-compose.yml
                sh 'SPRING_RABBITMQ_HOST=rabbitmq ./mvnw test'
            }
        }
// --- STAGE 2: Build (se o teste passar) ---
        stage('Build Docker Image') {
            steps {
                script {
                     // --- Estratégia profissional para tageamento ---
                     // Pega o pequeno hash git do commit para criar uma única tag rastreável
                     def commitHash = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                     def uniqueImageTag = "welington/sc-mensageria:${commitHash}"
                     def imageName = "welington/sc-mensageria"

                     echo "Construindo imagem com tag unica: ${uniqueImageTag}"

                     // Constroi a imagem usando a tag unica.
                     // o '.' significa 'use o diretorio corrente como contexto de build'
                     docker.build(uniqueImageTag, '.')
                     echo "Imagem construida com sucesso, com a tag ${uniqueImageTag}."

                     // Também aplique a tag 'latest' para a mesma imagem por conveniencia.
                     def latestImageTag = "${imageName}:latest"
                     sh "docker tag ${uniqueImageTag} ${latestImageTag}"
                     echo "Aplicada a tag 'latest' para a imagem: ${latestImageTag}."
                }
            }
        }
         // stage('Test') { ... }
        // stage('Push to Registry') { ... }
        // stage('Deploy') { ... }
    }
}