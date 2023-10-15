FROM openjdk:19-jdk-alpine
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=target/*.jar
COPY ./target/ca-provider-1.2.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]