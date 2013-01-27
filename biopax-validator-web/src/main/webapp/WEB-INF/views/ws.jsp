<%--
  #%L
  BioPAX Validator Web Application
  %%
  Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as 
  published by the Free Software Foundation, either version 3 of the 
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Lesser Public License for more details.
  
  You should have received a copy of the GNU General Lesser Public 
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/lgpl-3.0.html>.
  #L%
  --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
   <meta http-equiv="content-type" content="text/html;charset=utf-8" />
	<meta name="author" content="BioPAX" />
	<meta name="description" content="BioPAX Validator" />
	<meta name="keywords" content="BioPAX, Validation, Validator, Rule, OWL, Exchange" />
	<link rel="stylesheet" type="text/css" href="styles/style.css" media="screen" />
	<link rel="shortcut icon" href="images/favicon.ico" />
	<script type="text/javascript" src="scripts/rel.js"></script>
	<title>Webservice Description</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">


<h2>BioPAX Validator Webservice</h2>

<br/>

<div>

<h3>Check</h3>
To validate and, optionally, auto-fix and normalize local or remote BioPAX file(s), 
submit a multipart/form-data HTTP POST request to <a href="<c:url value='/check.html'/>">this page</a>

<h4>Parameters:</h4>
<ul>
<li><em>file</em> (actually, parameter name does not matter here, - simply submit an array of files) OR <em>url</em> (value: a URL to data in BioPAX format)</li>
<li><em>retDesired</em> - output format; values: "html" (default), "xml", or "owl" (modified BioPAX only, no error messages)</li>
<li><em>autofix</em> - false/true; try to fix BioPAX errors automatically and then normalize (default is "false")</li>
<li><em>filter</em> - set log level; values: "WARNING" (default, get both errors and warnings), "ERROR", and "IGNORE" (no problems at all ;))</li>
<li><em>maxErrors</em> - set the max. number of not fixed ERROR type cases to collect (some, but not all, warning and fixed cases will be also reported);
value: a positive integer; "0" (default) means "unlimited", "1" - fail-fast mode, i.e., stop after the first serious issue, "10" - collect up to ten error cases, etc.</li>
<li><em>profile</em> - use an alternative, pre-configured validation profile; currently, there is only one value available: "notstrict" (for particular rules to report 'warning' or nothing instead of 'error' - in the default configuration)</li>
</ul>

<h4>Output Formats:</h4>
<ul>
<li>HTML - stand-alone HTML/JavaScript validation results page to save and view off-line</li>
<li>XML - results follow the <a href="<c:url value='/schema.html'/>">schema (XSD)</a> 
 There are different ways to convert the XML result to domain objects. JAXB works great (one can generate the classes from the schema; 
copy/modify the classes used by the Validator (<a href="http://biopax.hg.sf.net/hgweb/biopax/validator/file/default/biopax-validator-core/src/main/java/org/biopax/validator/result/">sources here</a>);
 or grab into your project the (latest snapshot) jar (<a href="http://biopax.sourceforge.net/m2repo/snapshots/org/biopax/validator/biopax-validator-core/2.0-SNAPSHOT/">from the BioPAX public repository</a>) 
 or simply add that repository and the org.biopax.validator:biopax-validator-core:2.0-SNAPSHOT:jar dependency to your pom.xml 
 (though, this will automatically bring more dependencies to your project, e.g., paxtools-core, spring framework, etc...)</li>
<li>OWL - modified BioPAX L3 data (fixed/normalized)</li>
</ul>

</div>

<br/>

<div>
As an example, there is a basic BioPAX validator client module (it connects to the http://www.biopax.org/biopax-validator/check.html), and the 
<a href="http://biopax.hg.sf.net/hgweb/biopax/validator/file/default/biopax-validator-client">sources are here</a> (see test classes there as well:).
</div>
	
      </div>
    <div id="right">
      <jsp:include page="/templates/menu.jsp"/>
      <jsp:include page="/templates/box.jsp"/>
    </div>
    <div id="clear"></div>
  </div>

  <jsp:include page="/templates/footer.jsp"/>
</div>

</body>
</html>