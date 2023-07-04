# todo: move this to ../dist and update to use server.sh and LTW agent properly
FROM eclipse-temurin:latest
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-server","-javaagent:spring-instrument-5.3.28.jar","-Djava.security.egd=file:/dev/./urandom", "-Xmx4g", "-jar","/app.jar"]
EXPOSE 8080
