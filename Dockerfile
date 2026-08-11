# Etapa de compilación
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY src .
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución final
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]