# The BioPAX Validator ${project.version}

[![Build Status](https://travis-ci.org/BioPAX/validator.svg?branch=master)](https://travis-ci.org/BioPAX/validator) 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/744577f72ed14794bf970c8e8dd3f57b)](https://www.codacy.com/app/IgorRodchenkov/validator?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BioPAX/validator&amp;utm_campaign=Badge_Grade)
[![Coverage Status](https://coveralls.io/repos/github/BioPAX/validator/badge.svg)](https://coveralls.io/github/BioPAX/validator) 
[![MIT licence](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

- [biopax.org](http://www.biopax.org)
- [official online validator](http://www.biopax.org/validator/)
- [and wiki](https://github.com/BioPAX/validator/wiki)

  The BioPAX Validator is a command line tool, Java library, and online
web service for BioPAX formatted pathway data validation. The validator
checks for more than a hundred BioPAX Level3 rules and best practices, 
provides human readable reports and can automatically fix some common 
mistakes in data (can also process Level1 and Level2 data, which are 
first auto-converted to the Level3, and then Level3 rules apply). 
The validator is in use by the BioPAX community and is continuously being
improved and expanded based on community feedback.

  BioPAX is a community developed standard language for integration, 
exchange and analysis of biological pathway data. BioPAX is defined 
in Web Ontology Language (OWL) and can represent a broad spectrum 
of biological processes including metabolic and signaling pathways, 
molecular interactions and gene networks. Pathguide.org lists the 
pathway databases and tools that support BioPAX.

## Usage

Download the latest ZIP distribution from 

http://www.biopax.org/downloads/validator/

Unpack and use (it also includes the WAR file). 

### Console (batch)

When run as

```$sh path/to/validate.sh [args]```

it prints a brief help message when no arguments provided. 

The following example is how to validate a file or all files in a directory: 

```sh validate.sh input_dir_or_file --profile=notstrict```

Validation results are saved to the **current** work directory.

For data files under ~100Mb, you can also use biopax-validator-client.jar, 
which can do faster, for it does not do initialization every time 
(loading large ontology files); e.g., try it without arguments for more information:

```java -jar biopax-validator-client.jar <in> <out> [optional parameters...]```

Client app parameters are slightly different from the command-line ones.

Smaller files validate quickly, however, actual time may vary for the same size data; 
it takes longer for networks that contain loops (e.g., in nextStep->PathwayStep sequence);

If you want to validate several files, it always much more efficient 
to copy them all in a directory and use that directory as the first 
parameter for the validate.sh. This is because Validator's initialization
is very time/resources consuming task (mainly, due to OBO files parsing); 
after it's done, next validations are performed much faster.

### Web service

```sh server.sh```

starts the BioPAX Validator app with built-in application server: 
go to [http://localhost:8080] in a browser.

Use --help parameter to see all the server options (e.g., httpPort, ajpPort)

## Developer notes

It's built with Java ${java.version} (requires 8 or above), Paxtools ${paxtools.version} (BioPAX Model API, Java library), 
Spring Framework ${spring.version}, OXM, @AspectJ, AOP, LTW, etc. Validation _Rules_ are java 
objects that implement _Rule<E>_ interface, extend _AbstractRule<E>_, where _E_
is usually either a BioPAX class or _Model_. Controlled vocabulary rules 
extend _AbstractCvRule_ and use _CvRestriction_ and OBO Ontology Manager 
(based on PSIDEV EBI sources) to lookup for valid ontology terms and synonyms.
Rules may call other rules, but usually it is not recommended, for they are 
better keep simple and independent. 

_Post-model_ validation mode is to check 
all the rules/objects after the BioPAX model is built (created in memory or 
read from a file). _Fail-fast_ mode is to fail short on critical BioPAX and 
RDF/XML errors during the model is being read and built (with Paxtools). So, 
the Validator turns Paxtools' default _fail-fast_ mode into _greedy_ validation 
mode to collect and report all the issues at once. Maximum number of (not 
auto-fixed) errors per validation/report can be configured. Various specific 
BioPAX error types, levels, categories, messages, cases are reported. 

Spring AOP, MessageSource, resource bundles, and OXM help collect the errors, 
translate to human-readable messages and write the validation report (xml or html).
Settings such as _behavior_ (level), error code, category and message templates
are configured via the resource bundles: rules.properties, codes.properties and 
profiles.properties (e.g., /rules_fr_CA.properties can be added to see messages 
in French). 

To disable LTW AOP, set ```<context:load-time-weaver aspectj-weaving="off"/>```
in the applicationContext.xml or edit the META-INF/aop.xml to "physically" 
exclude any rule from being checked - in java source file comment out the 
@Component annotation (the corresponding singleton rule won't be automatically 
created nor added to the validator bean). Set ```<ruleClass>.behavior=ignore``` 
in the profiles.properties file (good for testing AOP and beans configuration 
without real validation job).

You can also edit links in obo.properties file and classpath in validate.sh 
script to use alternative OBO files (e.g., the latest). However, when an ontology
is unavailable or broken, the validator fails with a message: 
```text
Caused by: psidev.ontology_manager.impl.OntologyLoaderException: Failed loading/parsing ontology CL 
from http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/cell_type/cell.obo
```
(check the logs; try with another revision/location of failing ontology,
or revert to the default configuration)

The validator-core module is not specific to BioPAX; 
it could be used for another domain with alternative validation rules module.
