Additional configuration for the Tomcat app.

The default destination of the context.xml file is src/main/webapp/META-INF 
(ends up in /META-INF/ when deployed), and this could be placed right there in 
the project unless it were tomcat-specific and also conflicting with the 
tomcat6-maven-plugin/spring-instrument-tomcat/classLoaderClass 
configuration there (which allows for tomcat6:run command) in the pom.xml; 
that's why it is placed here, and then - copied to its proper location 
using maven-war-plugin trick.

