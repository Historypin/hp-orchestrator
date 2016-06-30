# Console web-app

Software requirements: Oracle Java 8, Apache Tomcat 8, Apache Maven 3.3, PostgreSQL 9.4, configured SMTP server

## Build
Run `mvn clean install` in main project directory to build project.

## Deployment

### Database
Directory {project_dir}/console/src/main/resources/db contains SQL scripts, which needs to be run before application is run in container.
1. Connect into your postgres installation and run commands in `create_DB_and_user_commands.txt` file to create database `console`, with user `console`.
2. In database `console` run SQL script in file `create_schema.sql` to create database schema. (psql -U console -W -h localhost -d console -f console/src/main/resources/db/create_schema.sql)
3. Run SQL script in file `import.sql` to initialize DB data. (psql -U console -W -hlocalhost -d console -f console/src/main/resources/db/import.sql)


### Webapp
Build will produce file `console-*.war` in {project_dir}/console/target which needs to be deployed into standard Tomcat servlet container.
After successful deployment application is available at url `http://localhost:8080/` (may depend on your tomcat run port and app context).

#### Default credentials
Username: admin

Password: admin

### Deploy script
File `{project_dir}/scripts/deploy.sh` contains deploy script used in CI. It only deploys new version of `console.war`! Database changes, and others needs to be performed manually.

## Environment properties
Application supports to run in different profiles, depending on target environment: development, test or production. It will use different property files for each profile, and some aspects as logging and caching will behave differently in each profile.

Environment is specified by runtime argument. If tomcat is run with JVM argument `-Dspring.profiles.active=dev`, application will be run in development mode,  and `-Dspring.profiles.active=test` is for test environment. If no argument is specified, application will assume it is run in production mode.

For now, only dev profile is supported.

## REST documentation
Rest api documentations is available at `http://mva.eea.sk:8080/swagger-ui.html` after successful login.

## Known problems

### Application deployment in tomcat takes too long
This issue has occurred during deployment on HP machine in the cloud infrastructure. On out machines, this issue has not occurred. To generate session tokens,
tomcat needs to feed its random number generator. If server is short of entropy, it takes a long time to initialize PRNG. This can be solved with using different source of "randomness".
In linux machines, run tomcat with parameter: `-Djava.security.egd=file:/dev/./urandom`. More about this issue: https://wiki.apache.org/tomcat/HowTo/FasterStartUp#Entropy_Source.
