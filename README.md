# IELTS System (ielts-system)

## Project Structure

```
ielts-system/
├── .gitattributes
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
├── .mvn/
│   └── wrapper/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── ddhva/
│   │               └── ielts/
│   │                   ├── config/
│   │                   ├── controller/
│   │                   ├── dto/
│   │                   ├── enums/
│   │                   ├── model/
│   │                   ├── repositories/
│   │                   ├── service/
│   │                   │   ├── crawler/
│   │                   │   ├── exception/
│   │                   │   └── impl/
│   │                   ├── util/
│   │                   └── IeltsApplication.java
│   └── test/
└── uploads/
    └── avatars/
```

## Description

**ielts-system** is a backend project, built with Java (Spring Boot), designed to support IELTS-related functionalities. The project follows a clean, modular package structure for scalability and maintainability.

## How to Run

1. Make sure you have Java and Maven installed.
2. Build and run the project:
   ```
   ./mvnw spring-boot:run
   ```
   or on Windows:
   ```
   mvnw.cmd spring-boot:run
   ```

## Main Features

- Java backend with Maven build system
- Clean, modular package structure (config, controller, dto, enums, model, repositories, service, util)
- Service layer with submodules: crawler, exception, impl
- Uploads directory for storing files (e.g., avatars)
