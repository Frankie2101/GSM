#
# Stage 1: Build the application using a Maven container.
# This stage compiles the Java code and packages it into a .jar file.
#
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#
# Stage 2: Run the application using a lightweight Java Runtime container.
# This stage creates the final, smaller image for deployment.
#
FROM openjdk:11-jre-slim
WORKDIR /app

#
# Copy only the built .jar file from the 'build' stage into the final image.
# This is the key to a small and efficient final image.
#
COPY --from=build /app/target/*.jar app.jar

#
# The command to execute when the container starts.
#
ENTRYPOINT ["java","-jar","app.jar"]