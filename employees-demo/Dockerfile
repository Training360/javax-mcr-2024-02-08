FROM eclipse-temurin:17.0.10_7-jdk

WORKDIR app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]