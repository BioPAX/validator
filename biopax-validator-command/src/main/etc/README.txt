/** BioPAX Validator, Version 2.0A (SNAPSHOT)
 **
 ** Copyright (c) 2010 University of Toronto (UofT)
 ** and Memorial Sloan-Kettering Cancer Center (MSKCC).
 **
 ** This is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** both UofT and MSKCC have no obligations to provide maintenance, 
 ** support, updates, enhancements or modifications.  In no event shall
 ** UofT or MSKCC be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** UofT or MSKCC have been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this software; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA;
 ** or find it at http://www.fsf.org/ or http://www.gnu.org.
 **/

An open source validation framework for BioPAX (www.biopax.org).

This is basically created with Java (5 or 6), Paxtools, Spring Framework (AOP,
MVC), and other open source jars. This implementation targets the 
BioPAX Level 3, but also it does support previous levels: Level 1 is auto-
converted to L2, and L2 is validated alike the L3, but not all L2 rules have 
been implemented.

Project URL: http://sourceforge.net/projects/biopax/


******************************************************************************
DESIGN
******************************************************************************

I. Framework

 1. Uses Paxtools library, the BioPAX API.
 2. Spring Framework (3.0) - to report errors (AOP), wire different modules,
    internationalize, and build web services (MVC).
 3. Java 6 (or 5): VarArgs, generics, annotations, AOP load-time weaving (LTW),
 	@Resource and @PostConstruct annotations.
 4. @AspectJ AOP, LTW are very powerful things that much simplify 
 	intercepting the exceptions in paxtools.jar and other external
    libraries (e.g., in Jena RDF parser). 

II. Validation

 1. Rules are java classes implementing Rule<E> interface, basically,
    by extending AbstractRule<E> (there are more abstract classes to derive from).

     Classes can use a modified "Ontology Manager" (PSIDEV@EBI, thank you!)
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
    - commons-logging and log4j
 2. Errors 
    - Spring AOP, MessageSource, resource bundles, and OXM
    are used to collect errors, translate into messages, and create the validation report.
 4. Control of behavior 
    - action and messages are configured in messages.properties file
 5. Some of errors can be ignored during the model import (e.g. cardinality constraints).



******************************************************************************
 INSTALL
******************************************************************************

Download the latest BioPAX validator ZIP distribution from 

http://sourceforge.net/projects/biopax/files/

Unpack 


******************************************************************************
 USING CONSOLE APPLICATION
******************************************************************************

Execute:
(switching to the validator directory is not required)

$sh /path/validate.sh [args]

(It prints a brief help when there are no arguments provided; 
e.g., run as "sh path1/validate.sh path2/input-dir path3/output.xml auto-fix normalize return-biopax")

For the input, one can use:
path/dir - to check all the OWL files in the directory (is probably the best choice)
file:path/file.owl - to check a single file;
classpath:path/file.owl - to check a file that can be found in java classpath (currently it's relative to the 'build' directory).
http://www.link.to/somer-biopax/ - to validate from a URL resource (remote file or service)
list:input.txt - execute lines, each like the above, from the specified 'batch' file

Validation messages will be printed to STDERR, unless user specified the output file.

NOTEs:

Smaller files validate quickly, however, actual time may vary for the same size data; 
it takes longer for networks that contain loops (e.g., in nextStep->PathwayStep sequence);
e.g., it took me several hours to validate ~50Mb BioPAX L3 file (Reactome's Canis familiaris);

If you want to validate several files, it always much more efficient 
to copy them all in a directory to use that directory as the first 
parameter for the validate.sh. This is because Validator's initialization
is very time/resources consuming task (mainly, due to OBO files parsing); 
after it's done, next validations are performed much faster.

******************************************************************************
USING WEB APPLICATION
******************************************************************************

PREREQUISITES:

- Java 6
- spring-instrument.jar (from Spring 3; enables load-time weaving for AOP)
- Tomcat (6) must be started with the following option: 

-Xmx2048m -Xms256m -Dfile.encoding=UTF-8 -javaagent:/full-path-to/spring-instrument.jar

(one can set the JAVA_OPTS variable and/or modify the Tomcat's startup script)


DEPLOY

Build from sources or download and rename the biopax-validator-2.0*.war to, e.g., biopax-validator.war, 
and simply copy to your Tomcat 'webapps' directory.

Note: Optionally, inside the WAR or in the 'webapps' directory (after deploying there), 
one can modify security settings in WEB-INF/users.properties and WEB-INF/security-config.xml
(secured access is used to change validation rules behavior at runtime);

Once it gets auto-deployed (also check server logs),
open http://localhost/biopax-validator with a browser. 


UNDEPLOY

If you delete the biopax-validator.war from the 'webapps', 
it is usually uninstalled automatically by Tomcat. 



******************************************************************************
 DEVELOPER NOTES
******************************************************************************

The most important thing is to make sure the validator 
starts using Java 6 with, e.g., the following JVM options:

-javaagent:lib/spring-instrument.jar -Xmx2048m -Xms256m -Dfile.encoding=UTF-8

(one may have to provide the full path to the spring-instrument.jar)

All the needed jars are in the /lib folder.

Package net.biomolecules.miriam (and classes in it) is xjc-generated.

Debugging tips:
- to disable LTW AOP, set <context:load-time-weaver aspectj-weaving="off"/> 
  in the applicationContext.xml; or edit the META-INF/aop.xml
- to "physically" exclude any rule from being checked - in java source file
   comment out the @Component annotation (the bean won't be auto-created).
- set "<ruleName>.behavior=ignore" in the messages.properties file
   (good to test AOP and configuration without doing real validation).
- disable BehaviorAspect in order all rules to always check
