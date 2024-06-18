# Use an official Maven image as a parent image
FROM maven:3.8.1-openjdk-11 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project file
COPY pom.xml .

# Download dependencies and build the project
RUN mvn dependency:go-offline

# Copy the project source
COPY src ./src

# Build the application
RUN mvn package

# Use AdoptOpenJDK as a base image for the final runtime image
FROM adoptopenjdk:11-jre-hotspot

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/RMManagementPortal.war ./RMManagementPortal.war

# Command to run the application
CMD ["java", "-jar", "RMManagementPortal.war"]
