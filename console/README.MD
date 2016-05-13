# Console web-app

Software requirements: Java 8, Apache Tomcat 8, Apache Maven 3, Postgres 9.4, Mule ESB 3.7.

## Build
Run `mvn clean install` in main project directory to build project.

## Deployment

### Database
Directory {project_dir}/console/src/main/resources/db contains SQL scripts, which needs to be run before application is run in container.
1. Connect into your postgres installation and run commands in `create_DB_and_user_commands.txt` file to create database `console`, with user `console`.
2. In database `console` run SQL script in file `create_schema.sql` to create database schema.
3. Run SQL script in file `import.sql` to initialize DB data.


### Webapp
Build will produce file `console-*.war` in {project_dir}/console/target which needs to be deployed into standard Tomcat servlet container.
After successful deployment application is available at url `http://localhost:8080/` (may depend on your tomcat run port and app context).

#### Default credentials
Username: admin

Password: admin

## Environment properties
Application supports to run in different profiles, depending on target environment: development, test or production. It will use different property files for each profile, and some aspects as logging and caching will behave differently in each profile.

Environment is specified by runtime argument. If tomcat is run with JVM argument `-Dspring.profiles.active=dev`, application will be run in development mode,  and `-Dspring.profiles.active=test` is for test environment. If no argument is specified, application will assume it is run in production mode.

## REST documentation
Rest api documentations is available at `http://mva.eea.sk:8080/swagger-ui.html` after successful login.
