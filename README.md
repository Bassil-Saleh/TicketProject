# Ticket Project

## Spring Boot Dependencies:

- Spring Web
- Thymeleaf
- MariaDB Driver
- Spring Data JPA

## Project Setup:

1. Clone this Git repository to your local machine.
2. Create a MariaDB database on your local machine.
3. If the file `src/main/resources/application.properties` doesn't already exist, create it with the following contents:

```
# Add this line to the top of the file
spring.application.name=webapp

# Add your MariaDB database name, username and password
spring.datasource.url=jdbc:mariadb://localhost:3306/your_database_name
spring.datasource.username=your_username
spring.datasource.password=your_password

# MariaDB Driver
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# update -> update schema to match entities
# create -> drop all tables and recreate them on every startup
# create-drop -> like create, but also drops on shutdown
# validate -> check if entities match the schema, throw an error if there's a mismatch
# none -> do nothing
spring.jpa.hibernate.ddl-auto=update

# JPA options (feel free to edit as you wish)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect
```