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

<h3>Endpoints (RESTful)</h3>
<ol>
<li>Check (and, optionally, auto-fix and normalize) either BioPAX L3 files or a resource, 
send a multipart/form-data HTTP POST request to <a href="<c:url value='/check.html'/>">this page</a>
</li>
</ol>

<h3>Parameters:</h3>
<ul>
<li><em>file</em> (actually, parameter name does not matter here, - simply submit an array of files) OR <em>url</em> (value: URL string)</li>
<li><em>retDesired</em> - output format: "html" (default), "xml", or "owl"</li>
<li><em>autofix</em> - false/true; try to fix some errors automatically (a new experimental feature; default is "false")</li>
<li><em>normalize</em> - false/true; return "normalized" BioPAX (a new experimental feature; default is "false")</li>
<li><em>filter</em> - set errors level; values: "WARNING" (default, get errors and warnings), "ERROR", "IGNORE" (no problems at all ;))</li>
</ul>

<h3>Output Formats:</h3>
<ul>
<li>HTML - stand-alone HTML/JavaScript result page that can be also saved and viewed off-line</li>
<li>XML - results follow the <a href="<c:url value='/schema.html'/>">schema (XSD)</a> 
 There are different ways to convert the XML result to domain objects. JAXB works great (one can generate the classes from the schema; 
copy/modify the classes used by the Validator (<a href="http://biopax.hg.sf.net/hgweb/biopax/validator/file/default/biopax-validator-core/src/main/java/org/biopax/validator/result/">sources here</a>);
 or grab into your project the (latest snapshot) jar (<a href="http://biopax.sourceforge.net/m2repo/snapshots/org/biopax/validator/biopax-validator-core/2.0-SNAPSHOT/">from the BioPAX public repository</a>) 
 or simply add that repository and the org.biopax.validator:biopax-validator-core:2.0-SNAPSHOT:jar dependency to your pom.xml (though, this will automatically bring more dependencies to your project, e.g., paxtools-core, spring framework, etc...)

 </li>
<li>OWL - BioPAX L3 (fixed/normalized)</li>
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