# PasswordApp


Overview:
Simple HTTP Server that services two endpoints: 1) /hash  2) /stats. 

Build Instructions:
1) Download source and change directory to the source folder
2) Build using maven: mvn clean package. In order to skip the test: mvn clean package -DskipTests

Execute Server:
1) If the build is successful, binary will be present in the 'target' folder
2) Run: jav -jar target/password-app-1.0-SNAPSHOT.jar

Application Properties:
The application looks for application.properties for configuration parameters. 
Default application.properties file is present under 'resources' directore. 

Example configuration:
inetAddress=localhost
port=8080
#System default is 0
tcpbacklog=0
maxPasswordLenBytes=1024
responseDelaySec=5
nThreads=10

tcpbacklog - Number of tcp connections that can back logged
maxPasswordLenBytes - maximum length of the password
responseDelaySec - delay to process /hash endpoint
nThreads - number of server threads

