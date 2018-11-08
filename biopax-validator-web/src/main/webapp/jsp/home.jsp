<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
	<title>The BioPAX Validator</title>
	<jsp:include page="head.jsp"/>	
</head>

<body>

<jsp:include page="header.jsp"/>

<h2>The BioPAX Validator</h2>

<div class="row">
<div class="jumbotron">
<p>
High quality biological pathway knowledge is easier to analyze and integrate  
at a greater level of detail and with standard software.
The BioPAX Validator applies dozens of custom criteria, some of which 
cannot be expressed in OWL or rule definition languages, to deal with 
syntax and semantic errors introduced in the course of pathway data modeling.
Rules originate from the BioPAX Level3 specification and the community best practice, 
are expressed as generic Java classes built around the 
<a href="http://www.ncbi.nlm.nih.gov/pubmed/24068901" target="_blank">Paxtools</a> API 
and Spring framework, report errors or warnings in several categories,  
and can optionally normalize BioPAX models, always auto-converting BioPAX Level 1, 2 to 3.
There are also console and Java library version of this tool.
</p>
<a href="check.html" class="btn btn-primary btn-large">Start Validating</a>
</div>
</div>

<div class="row">
<h4>How to cite:</h4>
	<span>Igor Rodchenkov, Emek Demir, Chris Sander, and Gary D. Bader. </span>
	<span><strong>The BioPAX Validator. </strong></span>
	<cite><abbr title="Bioinformatics">Bioinformatics</abbr> 
		(2013) 29 (20): 2659-2660 first published online August 5, 2013 
		doi: 10.1093/bioinformatics/btt452
	</cite>
	<ul class="list-inline">
		<li><a target="_blank" href="http://bioinformatics.oxfordjournals.org/content/29/20/2659">Abstract</a></li>
		<li><a target="_blank" href="http://www.ncbi.nlm.nih.gov/pubmed/23918249">PubMed entry</a></li>
	</ul>
	<span>Open Access</span>
</div>

<div class="row">
<h4>Feedback</h4>
<p>
Please comment or report issues to the 
<a target="_blank" href="https://github.com/BioPAX/validator">BioPAX Validator project</a> on GitHub.
</p>
</div>

<jsp:include page="footer.jsp"/>

</body>
</html>
