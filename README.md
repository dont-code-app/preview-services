![image](https://dont-code.net/assets/logo-shadow-squared.png)
## What is it for ?

These services are the back-end part of the Previewer. They receive application changes (when called by [Ide Services](https://github.com/dont-code/ide-services)) and push them to the Previewer Angular application using WebSockets.
They are part of the [dont-code](https://dont-code.net) no-code / low-code platform enabling you to quickly produce your very own application.

## What is it ?
These services are developed in [Quarkus](https://quarkus.io).

## How is it working ?
Preview services are Rest Services providing WebSockets support to communicate with the client application in realtime.

## How to build it ?
This project is a standard maven project:

1. Installing

   Download and Install [Maven](https://maven.org) if necessary.

2. Running tests

   `mvn test`

3. Building

   `mvn package` produces the Uber Jar preview-services-runner.jar

4. Running in dev mode enabling lib coding
   
    `mvn quarkus:dev` to start the services
   
    `http://localhost:8081` to access the homepage

4. Running in production mode

   `java -jar target/preview-services-runner.jar`

## Thank you

This project was generated using [Quarkus io generator](https://code.quarkus.io/).
