/** BioPAX Validator, Version 2.0.0
 **
 ** Copyright (c) 2009-2011 University of Toronto (UofT)
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

This is basically created with Java 6, Paxtools, Spring Framework (AOP,
MVC), and other open source jars. This implementation targets the 
BioPAX Level 3, but also it does support previous levels: Level 1 is auto-
converted to L2, and L2 is validated alike the L3, but not all L2 rules have 
been implemented. Optionally (when 'normalize' flag is set), L2 data can be 
converted to the L3, first, and then the result is validated and normalized.

Project URL: 
http://sourceforge.net/projects/biopax/
http://www.biopax.org/validation

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
(note: switching to the validator directory is not required)

$sh path/to/validate.sh [args]

(It prints a brief help when there are no arguments provided; 
e.g., run as "sh path1/validate.sh path2/input-dir path3/output.xml auto-fix normalize return-biopax")

For the input, one can use:
path/dir - to check all the OWL files in the directory (it is probably the best pick)
file:path/file.owl - to check a single file;
classpath:path/file.owl - to check a file that can be found in java classpath (currently it's relative to the 'build' directory).
http://www.link.to/somer-biopax/ - to validate from a URL resource (remote file or service)
list:input.txt - execute lines, each like the above, from the specified 'batch' file

Validation messages will be printed to STDERR, unless user specified the output file.

NOTEs:

Smaller files validate quickly, however, actual time may vary for the same size data; 
it takes longer for networks that contain loops (e.g., in nextStep->PathwayStep sequence);
e.g., once it took me several hours to validate ~50Mb BioPAX L3 file (Reactome's Canis familiaris);

If you want to validate several files, it always much more efficient 
to copy them all in a directory and use that directory as the first 
parameter for the validate.sh. This is because Validator's initialization
is very time/resources consuming task (mainly, due to OBO files parsing); 
after it's done, next validations are performed much faster.

One CAN edit the URLs in the obo.properties file to use alternative OBO ontologies/revisions 
(e.g., this helps when an ontology resource becomes unavailable or broken, e.g., the latest revision introduces bugs, etc...;
i.e, if the validator fails for this reason, look for a message like "Caused by: psidev.ontology_manager.impl.OntologyLoaderException: 
Failed loading/parsing ontology CL from http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/cell_type/cell.obo"
in the console output and try with another revision of that ontology.)


******************************************************************************
USING WEB APPLICATION
******************************************************************************

PREREQUISITES:

- Java 6
- spring-instrument.jar (from Spring 3; enables AspectJ load-time weaving)
- Tomcat (6) must be started with the following option: 

-Xmx2048m -Xms256m -Dfile.encoding=UTF-8 -javaagent:/full-path-to/spring-instrument.jar

(one can set the JAVA_OPTS variable and/or modify the Tomcat's startup script)


DEPLOY

Build from sources or download and rename the biopax-validator-2.0.*.war to, e.g., biopax-validator.war, 
and simply copy to your Tomcat 'webapps' directory.

Note: Optionally, inside the WAR or in the 'webapps' directory (after deploying there), 
one can modify security settings in WEB-INF/users.properties and WEB-INF/security-config.xml
(secured access is used to change validation rules behavior at runtime);

Once it gets auto-deployed (also check server logs),
open http://localhost/biopax-validator with a browser. 

Also, one CAN edit URLs in the obo.properties file (usually - in the WEB-INF/classes) 
and restart the server to use other than default OBO ontologies/revisions 
(e.g., this helps when an ontology resource becomes unavailable or broken, 
e.g., the latest revision introduces bugs, etc..; i.e, if the validator fails to start for this reason, 
look for a log message like "Caused by: psidev.ontology_manager.impl.OntologyLoaderException: 
Failed loading/parsing ontology CL from http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/cell_type/cell.obo"
and try with another revision of that ontology.)

UNDEPLOY

If you delete the biopax-validator.war from the 'webapps', 
it is usually uninstalled automatically by Tomcat. 



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
    by extending AbstractRule<E> or its abstract sub-class.

    Controlled vocabulary rule classes depend on a modified "Ontology Manager" 
    (derived from one of PSIDEV software modules, thank you!)
    and its configuration file (obo.properties) to check 
    controlled vocabulary terms.
 
    Rules may call other rules, but usually it is not required/desired, 
    and they are better keep simple and independent.
    
 2. Post-model validation mode
    - read a BioPAX file, build the model, and check all the rules.
     
 3. Real-time (AOP) validation mode
    - catch/log all the "external" errors
    - catch/log syntax and BioPAX errors during the model is being read/built
    - check before/after any BioPAX element is modified or added to the model.
 4. Fail-fast mode (the number of not fixed errors per validation/report is limited)

III. Errors, Logging, Behavior (actions to undertake)
 1. Logging 
    - commons-logging and log4j
 2. Errors 
    - Spring AOP, MessageSource, resource bundles, and OXM (JAXB)
    are used to collect errors, translate into messages, and create the validation report.
 3. Configuring
    - behavior (i.e. level), mode, error categories and messages are configured in properties files
 4. Some of errors can be ignored during the model import (e.g. cardinality constraints).


******************************************************************************
 DEVELOPER NOTES
******************************************************************************

The most important thing is to make sure the validator 
starts using Java 6 with, e.g., the following JVM options:

-javaagent:spring-instrument.jar -Xmx2048m -Xms256m -Dfile.encoding=UTF-8

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
