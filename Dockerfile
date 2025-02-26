FROM gradle:jdk21 AS build
WORKDIR /Hotel_ML_Rooms_And_Hotels_Service
COPY . .
RUN chmod +x gradlew
RUN gradle clean bootJar && ls -lah build/libs

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /Hotel_ML_Rooms_And_Hotels_Service
COPY --from=build /Hotel_ML_Rooms_And_Hotels_Service/build/libs/*.jar Hotel_ML_Rooms_And_Hotels_Service.jar
ENTRYPOINT ["java", "-jar","Hotel_ML_Rooms_And_Hotels_Service/Hotel_ML_Rooms_And_Hotels_Service.jar"]