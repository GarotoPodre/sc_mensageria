# Stagio 1: A fase 'Build' - Constroi a aplicacao usando Maven
# Usando uma imagem com JDK 21 para corresponder ao pom.xml
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Definindo o diretorio de trabalho dentro do conteiner
WORKDIR /app

# Copiando o POM. Isso é feito primeiro para subir a camada de cache do Docker.
# Se não houver alteracoes no pom.xml, o Docker não ira baixa novamente as dependencias no bulds subsequentes.
COPY pom.xml .

# Copia os pacotes de arquivos mavem
COPY .mvn/ .mvn
COPY mvnw .
COPY mvnw.cmd .

# --- THE FIX ---
# Garante que o script do Maven Wrapper tenha permissão de execução.
# Isso é crucial, pois as permissões de arquivo podem ser perdidas durante o checkout do Git.
RUN chmod +x mvnw

# Copia o resto do codigo fonte da aplicacao.
COPY src ./src

# Roda o construtor maven para criar um JAR executavel.
# -DskipTests eh importante para CI/CD para separar as fases de build e testes.
RUN ./mvnw package -DskipTests


# Stage 2: A fase "Runtime" - Cria uma imagem leve e final
# Usando uma imagem JRE 21 para corresponder à versão de compilação
FROM eclipse-temurin:21-jre-jammy

# Definindo diretorio de trabalho
WORKDIR /app

ARG JAR_FILE=/app/target/*.jar

# Copia o arquivo JAR executavel do estágio "builder" usando ARG.
COPY --from=builder ${JAR_FILE} app.jar

EXPOSE 8088

# Comando para rodar a aplicacao quando o conteiner inicia.
ENTRYPOINT ["java", "-jar", "app.jar"]