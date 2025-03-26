FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 8080
COPY ./api/target/api-1.0-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]