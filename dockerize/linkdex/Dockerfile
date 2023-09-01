FROM openjdk:22-ea-slim
MAINTAINER linkdex:22-ea-slim
ARG JAR_FILE=target/Linkdex-2.0.jar
WORKDIR /opt/app
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]