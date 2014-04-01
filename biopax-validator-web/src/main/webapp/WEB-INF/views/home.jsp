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
at a much greater level of detail and with standard tools. 
To help detect and fix syntax and semantic issues introduced in the course of 
biological pathway data modeling and exporting to BioPAX, 
we created this BioPAX Validator, with dozens of custom criteria, 
some of which cannot be expressed in OWL or rule definition languages.
These rules originate from the BioPAX specification and the community best practice, 
are expressed in Java as generic classes built mostly around the 
<a href="http://www.ncbi.nlm.nih.gov/pubmed/24068901" target="_blank">Paxtools</a> API, 
and embraced by the aspect-oriented framework (AOP). It reports all the issues at once, 
in several categories and levels, and the normalized BioPAX Level3 model (optional),
auto-converting BioPAX Level 1, 2 to 3 if required (before it checks the rules).
There are also Java console and library version of the BioPAX Validator.
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
Please feel free to post your comments and suggestions at the 
<a target="_blank" href="http://www.biopax.org/wiki/index.php?title=BioPAXValidator">Validator Wiki</a> or 
<a target="_blank" href="http://sourceforge.net/p/biopax/_list/tickets">BioPAX issue tracker</a>.
</p>
</div>

<jsp:include page="footer.jsp"/>

</body>
</html>
