FROM eclipse-temurin:latest
VOLUME /tmp
ARG APP_JAR
ARG AGENT_JAR
COPY ${APP_JAR} app.war
COPY ${AGENT_JAR} agent.jar
ENV JDK_JAVA_OPTIONS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
ENTRYPOINT ["java", "-server", "-javaagent:agent.jar", \
"-Djava.security.egd=file:/dev/./urandom", "-Xmx4g", "-Dfile.encoding=UTF-8", "-jar", "app.war", "--server.port=8080"]
EXPOSE 8080
