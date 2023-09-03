<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
	<title>BioPAX Validator: web service</title>
	<jsp:include page="head.jsp"/>
</head>
<body>

<jsp:include page="header.jsp"/>

<h2>The BioPAX Validator Web Services</h2>

<div class="row">
<h3>Check</h3>
	<p>To validate and, optionally, auto-fix and normalize local or remote BioPAX file(s),
		submit a multipart/form-data HTTP POST request to
		<a class="btn btn-success" href="check">this URL</a>.</p>
</div>

<div class="row">
<h4>Parameters:</h4>
<ul>
<li><em>file</em> (actually, parameter name does not matter here, - simply 
submit an array of files) OR <em>url</em> (value: a URL to data in BioPAX format)</li>
<li><em>retDesired</em> - output format; values: "html" (default), "xml", 
or "owl" (modified BioPAX only, no error messages)</li>
<li><em>autofix</em> - false/true; try to fix BioPAX errors automatically 
and then normalize (default is "false")</li>
<li><em>filter</em> - set log level; values: "WARNING" (default, get both 
errors and warnings), "ERROR", and "IGNORE" (no problems at all ;))</li>
<li><em>maxErrors</em> - set the max. number of not fixed ERROR type cases 
to collect (some, but not all, warning and fixed cases will be also reported);
value: a positive integer; "0" (default) means "unlimited", "1" - fail-fast 
mode, i.e., stop after the first serious issue, "10" - collect up to ten error cases, etc.</li>
<li><em>profile</em> - use an alternative, pre-configured validation profile; 
currently, there is only one value available: "notstrict" (for particular 
rules to report 'warning' or nothing instead of 'error' - in the default 
configuration)</li>
</ul>
</div>

<div class="row">
<h4>Output Formats:</h4>
<ul>
<li>HTML - stand-alone HTML+JavaScript validation results page 
to save locally and view off-line</li>
<li>XML - results in the XML format defined by the 
<a target="_blank" href="schema">XML schema</a></li>
<li>OWL - modified BioPAX L3 data (fixed and normalized)</li>
</ul>
</div>

<div class="row">
As an example, there is a basic BioPAX validator client module 
(it connects to the http://www.biopax.org/validator/check), and the
<a target="_blank" href="https://github.com/BioPAX/validator/tree/master/biopax-validator-client">
sources are here</a> (see test classes there as well:).
</div>

<jsp:include page="footer.jsp"/>

</body>
</html>