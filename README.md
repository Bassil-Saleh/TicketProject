# Ticket Project

## Spring Boot Dependencies:

- Spring Web
- Thymeleaf
- MariaDB Driver
- Spring Data JPA

## Project Setup:

1. Clone this Git repository to your local machine.
2. Create a MariaDB database on your local machine.
3. Use the following command to generate three secret encryption keys in Base64 encoding.
They will be used by the application for encrypting/decrypting sensitive information stored
in some database tables, as well as for computing blind indexes:

```
openssl rand -base64 32
```

4. If the file `src/main/resources/application.properties` doesn't already exist, create it with the following contents:

```
# Add this line to the top of the file
spring.application.name=webapp

# If you're running this application locally,
# you can set this to http://localhost:8080
app.config.base-url=base_url_to_your_machine

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

# For encrypting and encrypting database fields with sensitive information
app.encryption.key-base64=your_first_base64_encoded_secret_key_goes_here
# For computing blind indexes
app.blind-index.key-base64=your_second_base64_encoded_secret_key_goes_here
# For signing and authenticating JSON Web Tokens.
app.jwt.secret-base64=your_third_base64_encoded_secret_key_goes_here

# SMTP server config used by this application to send out emails to users
spring.mail.host=your_smtp_hostname_goes_here
spring.mail.port=your_smtp_port_goes_here
spring.mail.username=your_smtp_username_goes_here
spring.mail.password=your_smtp_password_goes_here
# Enable/disable SMTP authentication.
# Set this to true or false based on your needs.
spring.mail.properties.mail.smtp.auth=true_or_false
# Enable/disable StartTLS encryption.
# Set this to true or false based on your needs.
spring.mail.properties.mail.smtp.starttls.enable=true_or_false
```

5a. To run the application, use this command:
```
./mvnw spring-boot:run
```

5b. To run the application's tests, use this command:
```
./mvnw test
```