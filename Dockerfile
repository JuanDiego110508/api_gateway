# --- Etapa 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# 1. Compilar e instalar la librería común (wd-lib-common)
COPY wd-lib-common ./wd-lib-common
RUN mvn -f wd-lib-common/pom.xml clean install -DskipTests

# 2. Copiar pom y descargar dependencias de api_gateway (Caché de capas)
COPY api_gateway/pom.xml ./api_gateway/
RUN mvn -f api_gateway/pom.xml dependency:go-offline -B

# 3. Copiar código fuente de api_gateway y empaquetar
COPY api_gateway/src ./api_gateway/src
RUN mvn -f api_gateway/pom.xml clean package -DskipTests

# --- Etapa 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el artifact empaquetado desde la etapa de Build
COPY --from=builder /app/api_gateway/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]