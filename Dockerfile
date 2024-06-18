# Use a base image with Java and Tomcat
FROM tomcat:9.0.89-jdk11-openjdk

# Copy the WAR file to the webapps directory of Tomcat
COPY target/RMManagementPortal-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/

# Expose the port the application runs on
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]