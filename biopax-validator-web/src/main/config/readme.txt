Additional configuration for the Tomcat app.

context.xml - the default destination of this file is src/main/webapp/META-INF 
(ends up in /META-INF/ when deployed), and this could be placed right there in 
the project unless it conflicted with the tomcat6-maven-plugin/spring-instrument-tomcat/classLoaderClass 
configuration there (which allow one to use tomcat6:run command!) in the pom.xml; 
that's why it is placed here and then copied to the final destination using maven-war-plugin trick.

