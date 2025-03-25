
#!/bin/bash

echo "Starting vaccination management application in development mode..."

# Use Maven Wrapper to run Spring Boot with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
