
# Use the official Tomcat image as a base
FROM tomcat:9.0

# Copy the WAR file to the webapps directory of Tomcat
COPY target/RMManagementPortal-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/

# Expose the default Tomcat port
EXPOSE 8446

# Start Tomcat server
CMD ["catalina.sh", "run"]
