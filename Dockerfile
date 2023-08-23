FROM openjdk:17
MAINTAINER editory_v1.
ARG JAR_FILE=target/LocalSearchEngine-1.0.jar
WORKDIR /opt/app
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]