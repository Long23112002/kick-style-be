FROM maven:3.8.4-openjdk-17 AS build

WORKDIR /app
COPY . /app

RUN mvn dependency:go-offline
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
#FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/kickstyle-ecommerce-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 6868
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
