********************************
       BioPAX Validator
********************************

An open source validation framework for BioPAX, 
started by the Pathway Commons project team 
(www.pathwaycommons.org)

This is basically created with Java 6, Paxtools, 
Spring Framework (AOP, MVC), and other open source jars. 
This implementation targets the BioPAX Level 3, 
but it does also support previous levels as well: 
Level 1 is auto-converted to L2, and L2 is validated,
but, unlike for L3, not many L2 rules have been implemented.

Project URL: http://sourceforge.net/projects/biopax/

********************************
DESIGN
********************************

I. Framework

 1. Paxtools, the BioPAX API, is used as external library.
 2. Spring Framework 2.5.6 - to report errors (AOP), wire different modules,
    internationalize, and build web services (MVC).
 3. Java (5 or 6): VarArgs, generics, annotations, AOP load-time weaving (LTW),
 	@Resource and @PostConstruct annotations.
 4. @AspectJ AOP, LTW are very powerful things that much simplify 
 	intercepting the exceptions in paxtools.jar and other external
    libraries (e.g., in Jena RDF parser). 

II. Validation

 1. Rules are java classes implementing Rule<E> interface, basically,
    by extending AbstractRule<E> (there are more abstract classes to derive from).

     Classes can use the build-in "Ontology Manager" (PSIDEV@EBI, thank you!)
     and its configuration file (ontologies.xml) which allows to check 
     controlled vocabulary terms.
 
    Rules may call other rules, but usually it is not required, and they are quite simple.
    
 2. Post-model validation 
 	- read a BioPAX file, build the model, and check all the rules.
     
 3. Fast (AOP) validation
    - catch/log all the "external" errors ()
    - catch/log syntax and BioPAX errors during the model is being read/built
	- check before/after any BioPAX element is modified or added to the model.

III. Errors, Logging, Behavior (actions to undertake)
 1. Logging 
 	- commons-logging, log4j.jar, and log4j.xml
 2. Errors 
    - AOP, MessageSource (Spring), resource bundles and object-xml mapping (OXM) 
    are used to collect errors, translate into messages, and create the validation report.
 4. Control of behavior 
    - action and messages are configured in messages.properties file
 5. Some of errors can be ignored during the model import (e.g. cardinality constraints).



********************************
 INSTALL
********************************
Download the latest BioPAX validator ZIP release from: 

http://sourceforge.net/projects/biopax/files/

Unpack; switch to the directory to continue.

NOTE: default is that validator will download latest controlled vocabularies 
from the Ontology Lookup Service (OLS). You may change this behavior by
replacing the 'ontologies-remote.xml' with 'ontologies.xml' in the 
src/applicationContext.xml file.

********************************
 USING AS CONSOLE APPLICATION
********************************
Execute:

$sh validate.sh
(or "sh path1/validate.sh path2/input-dir path3/output.xml" 
- or without args, from any directory,..)

OR

$ ant run
(from the validator directory)

At some point the program asks for user input; and one can use:
path/dir - to check all the OWL files in the directory (is probably the best choice)
file:path/file.owl - to check a single file;
classpath:path/file.owl - to check a file that can be found in java classpath (currently it's relative to the 'build' directory).
http://www.link.to/somer-biopax/ - to validate from a URL resource (remote file or service)
pc:123456 - to retrieve and check a pathway from Pathway Commons
list:input.txt - execute lines, each like the above, from the specified 'batch' file

Validation messages will be written to the STDERR, unless user specified a file name.

You may provide any URL instead of file:.. 
(with little modification in the Main class it's possible to query for PC pathways)


********************************
USING AS WEB APPLICATION
********************************

PREREQUISITES:

- Java 6
- spring-agent.jar (from Spring 2.5.6; enables load-time weaving for AOP)
- Tomcat (6) must be started with the following option: 

-Xmx2048m -Xms256m -javaagent:/full-path-to/spring-agent.jar

(one can set the JAVA_OPTS variable and/or modify the Tomcat's startup script)

BUILD:

Edit, if required, the WEB-INF/web.xml configuration file.

If you plan accessing the validator SOAP web service programmatically, 
and from other computers (not only via http://localhost/biopax-validator/),
modify the <init-param> value of CXFServlet in the the Tomcat's 
WEB-INF/web.xml file. This is not required at all if you validate 
BioPAX files using the browser interface only.
(this guarantees that the generated WSDL and page (http://localhost:8080/biopax-validator/services/) 
contains correct URL):
...
  <servlet>
    <servlet-name>CXFServlet</servlet-name>
    <servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
    <init-param>
    	<param-name>base-address</param-name>
    	<param-value>http://localhost:8080/biopax-validator/services</param-value>
	</init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>
  
Configure Security: see WEB-INF/users.properties and WEB-INF/security-config.xml
(secured access is used to control validation rules behavior at runtime)

Finally, execute

$ant war


DEPLOY

Copy the generated biopax-validator.war to your Tomcat 'webapps' directory.

Once it gets auto-deployed (also check server logs),
open http://localhost/biopax-validator with a browser. 


UNDEPLOY

If you delete the biopax-validator.war from the 'webapps', 
it is usually uninstalled automatically by Tomcat. 



********************************
 DEVELOPER NOTES
********************************

The most important thing is to make sure the validator 
starts using Java 6 with the following JVM options:

-javaagent:lib/spring-agent.jar -Xmx2048m -Xms512m

(one may need to provide the full path to the spring-agent.jar)

It requires at least -Xmx1024m option, and the reason is 
that every time at startup the Ontology Manager parses and caches 
several OBO files (GO, PSI-MI, etc. ontologies)

All the needed jars are in the /lib folder.

Package net.biomolecules.miriam (and classes in it) is generated 
from the src/resources/Miriam.xsd using 'ant bind' command.

Debugging

- to disable LTW AOP - edit the META-INF/aop.xml
- to "physically" exclude a rule from being checked - in java source file
   comment out the @Component annotation (the bean won't be auto-created).
- set "<ruleName>.behavior=ignore" in the messages.properties file
   (good to test AOP and configuration without doing real validation).
- disable BehaviorAspect in order all rules to always check

Control the logging via log4j.xml:

    <logger name="org.biopax.validator" additivity="false">
        <level value="trace"/>
        <appender-ref ref="out"/>
    </logger>

 - to see AspectJ info use:
    <root>
	  <priority value="info"/>
	  <appender-ref ref="error"/>
    </root>

Cheers.