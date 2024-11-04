FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml ./

COPY repository/pom.xml repository/
COPY service/pom.xml service/
COPY web/pom.xml web/

RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline

COPY repository/src/main/java repository/src/main/java
COPY repository/src/main/resources repository/src/main/resources

COPY service/src/main/java service/src/main/java
COPY service/src/main/resources service/src/main/resources

COPY web/src/main/java web/src/main/java
COPY web/src/main/resources web/src/main/resources

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -pl web -am


FROM eclipse-temurin:17-jre-alpine
COPY --from=build /app/web/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]