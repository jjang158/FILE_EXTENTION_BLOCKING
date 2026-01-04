FROM maven:3.9.6-amazoncorretto-17 as build
WORKDIR /workspace/app

COPY pom.xml .
COPY src src

# Build the application
RUN mvn install -DskipTests

FROM amazoncorretto:17-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency

# Create a non-root group and user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
