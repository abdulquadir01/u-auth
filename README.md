## uAuth
- formally **u-register**
##### a small app to authenticate users & provide role based access.

### What this app does?
- authentication using jwt
- role based acess.
- [TBD] auth server capabilities.
- [TBD] login & signup pages.
- [TBD] dasboard/profile page for admin & other roles. 

<hr>

### How to run this application
- must have installed JDK17 ( as we're using SpringBoot3 )
- install MySQL8 or DB of your choice. (I'm using MySQL)
- configure DB connection realted info in the properties file.
- mvn 3.7 or above
- any IDE of your choice. 
- clone this repo, open in your IDE
- if you're using DB other than MySQL, modify the db config in application.properties file
- on termial type *./mvnw spring-boot:run* hit enter
- OR right click on UAuthApplication.java and select run

### What's new,
- migrated to Spring Boot 3.4
- code cleanup & refactoring