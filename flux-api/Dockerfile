FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /usr/src/app

RUN apk add --no-cache bash openssl

COPY mvnw    .
COPY .mvn    .mvn
RUN chmod +x mvnw

COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src              ./src
COPY generate_keys.sh .

RUN chmod +x generate_keys.sh && \
    bash ./generate_keys.sh

RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /usr/src/app/target/*.jar ./app.jar

RUN mkdir -p /app/keys
COPY --from=build /usr/src/app/src/main/resources/app.key      /app/keys/app.key
COPY --from=build /usr/src/app/src/main/resources/app.pub      /app/keys/app.pub

EXPOSE 8080

ENV JWT_PUBLIC_KEY_PATH=file:/app/keys/app.pub
ENV JWT_PRIVATE_KEY_PATH=file:/app/keys/app.key
ENV DEFAULT_ACCESS_ORIGIN=http://localhost

ENTRYPOINT ["java", "-jar", "app.jar"]
