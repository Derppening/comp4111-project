# COMP4111 Project

[Link to project description](https://course.cse.ust.hk/comp4111/project.html)

## Contribution Declaration

|               | TANG, Au Wa                           | MAK, Ching Hang               |
| ------------- | ------------------------------------- | ----------------------------- |
| Share of Work | 40%                                   | 60%                           |
| Best Five     | `*DataAccess`                         | `DatabaseConnectionPoolV2`    |
|               | `DatabaseConnection(Pool)` (not V2)   | `DatabaseConnectionV2`        |
|               | `SecurityUtils`                       | `TokenManager`                |
|               | `QueryUtils`                          | `HttpPathHandler`             |
|               | `AsyncServerRequestHandler`           | `HttpEndpointHandler`         |


## Prerequisites

- JDK 11
    - [Oracle JDK](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
    - [OpenJDK](https://jdk.java.net/java-se-ri/11)
- MySQL Server 5.7
    - A Docker container is available [here](docker-compose/mysql)

## Running the Project

This project contains several applications which may be run for various purposes.

Before executing any of the following examples, ensure that the default Java installation is set to JDK 11. See 
[FAQ](#faq) for more information.

### Running the Application

Before starting the application, ensure that the MySQL server has been started.

At the root of the project, run the following command:

```sh
./gradlew run
```

The application is running when an output similar to the following is displayed on the console:

```
<=========----> 75% EXECUTING [5s]
> :run
```

You should wait for the message `Listening on port[...]` before starting your testing.

### Running Custom Applications

This project contains several other applications which may be useful. You may run these via `./gradlew <name>`

- `runFreshDb`: Same as `run`, but drops and recreates the database.
- `runFreshTables`: Same as `run`, but drops and recreates all tables.
- `runDbInit`: Only recreate the database.
- `runTablesInit`: Only recreate the tables.
- `runDbDrop`: Only drop the database.

It is recommended to always run `runFreshDb` or `runFreshTables` to ensure that the database and/or tables are correctly
created, and that no additional data are present to mess up the test cases.

### Running Unit Tests

Unit tests can be run using the following command:

```sh
./gradlew test
```

Note that unit tests **WILL** recreate the database on every invocation. There is currently no way to change this.

### Running Infer

To perform a static analysis using infer, see [here](docker-compose/infer).

### Cleaning the Project

Run the following command:

```sh
./gradlew clean
```

## Project Structure

This project follows Gradle's directory layout convention.

- `build`: Build files and generated output
- `docker-compose`: Docker Compose files for encapsulating required services
- `src/main/java`: The main Java application
- `src/test/java`: The testing source files

## External Libraries

The following external libraries are used.

- [Apache HTTP Components 5.0](http://hc.apache.org/httpcomponents-core-ga/)
- [Apache Log4J 2.13](https://logging.apache.org/log4j/)
- [SLF4J 1.7](http://www.slf4j.org/)
- [Jetbrains Annotations](https://github.com/JetBrains/java-annotations)
- [Jackson JSON](https://github.com/FasterXML/jackson)
- [JUnit Jupiter](https://junit.org/junit5/)

## FAQ

### Gradle Java Version

If the following output appears when trying to build the application:

```
> Task :compileJava FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':compileJava'.
> Could not target platform: 'Java SE 11' using tool chain: 'JDK 8 (1.8)'.

```

That means your default Java version is older than JDK 11 as required by this project. Ensure that JDK 11 is installed 
on your system, and do one of the following:

- Add `-Dorg.gradle.java.home=<JDK_11_PATH>` to the `gradlew` invocation command-line, replacing `<JDK_11_PATH>` with 
the path to your JDK 11 installation
- Set the `JAVA_HOME` environment variable to your JDK 11 installation. Search Google for instructions specific to your 
OS and distro.

### Custom Database Info

You may change the database information in `src/main/resources/mysql.properties`.

- `mysql.url`: URL to the MySQL server
- `mysql.username`: Username to login to the server
- `mysql.password`: Password of the user
- `mysql.database`: Database name to use/create
