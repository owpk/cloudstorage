# Simple Cloud Storage App

An example of a simple file manager with the ability to exchange files between a remote server and a client. Supports drag-and-drop actions, simple authorization and sing up functions. 
The server implements the netty framework 4.1.51.

* See client.properties and server.properties files to configure the app

<a><img src="https://github.com/vzvz4/Test/blob/master/sc.jpg" width="500"/></a>

## Building

#### Prerequisites:

 1. Java (oracle jdk) 8.
    <p>Installation guide for Arch linux :
    <a href="https://linuxhint.com/install-oracle-jdk-10-on-arch-linux/">here, but you should download java 8 version</a></p>
 
#### Build
**Go to the project root and run**
```sh
    ./mvnw package
```
**Open client/target and server/target folder and run "jar-with-dependencies.jar" files by using console.**
**For example:**
```sh
    ...clint/target$ java -jar client-1.0-jar-with-dependencies.jar
```
**You should see "[main] INFO  org.owpk.core.Server - Server started at : 8190" message from the server and "connected : localhost/127.0.0.1:8190" message from the client if everything is ok.**
    
* By default, server creates a "clients_folders" directory in server root as default users files storage. Client creates a "Cloud Storage downloads" directory in client root as default download directory.   
* If you want to customize some properties like port address, download dirs etc, look at client.properties and server.properties files, you can find it in project root or in target folder if you have already built the app.
* The server uses MySQL connection to authorize and add a new user, in project root you can find a DDL script to build your own database server, connection properties described in /server/src/resources/META-INF/persistence.xml file.
* You can also connect to the server with "test user" login and password.

## Docker
* You can run the server in docker
  - Install Docker on the machine you want to run it.
  - Go to project root and run the commands below by using console
```sh
    $docker build -t java-app:cloud .
    $docker run java-app:cloud -p 8190:8190/tcp
        
    Or just pull it from repository
    $docker pull vzvz4/java-app:cloud
    And run it
    $docker run -p 8190:8190/tcp vzvz4/java-app:cloud
```
   

