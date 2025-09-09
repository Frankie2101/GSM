# Giai đoạn 1: Build ứng dụng với Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng với Java Runtime
FROM openjdk:11-jre-slim
WORKDIR /app
# Lấy file .jar đã được build từ giai đoạn 1
COPY --from=build /app/target/*.jar app.jar
# Lệnh để chạy ứng dụng khi container khởi động
ENTRYPOINT ["java","-jar","app.jar"]