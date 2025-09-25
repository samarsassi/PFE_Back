FROM eclipse-temurin:17-jre
WORKDIR /app

# Non-root user
RUN useradd --create-home --shell /bin/bash appuser && chown -R appuser /app
USER appuser

# Copy prebuilt fat jar from local target
COPY target/*.jar app.jar

# JVM options
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

# Spring Boot port
EXPOSE 8089

# Run
ENTRYPOINT ["/bin/sh","-c","java $JAVA_OPTS -jar app.jar"]
