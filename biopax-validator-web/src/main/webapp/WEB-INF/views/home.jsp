<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page language="java" contentType="text/html; charset=UTF-8"%>

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
<blockquote>
High quality biological pathway knowledge is easier to analyze and integrate  
at a much greater level of detail and with generic, standard tools. 
To help detect and fix syntax and non-trivial semantic issues 
introduced in the course of biological pathway data modeling, mapping, and exporting to BioPAX, 
we created the BioPAX Validator software, with dozens of custom criteria, 
some of which cannot be expressed in OWL or rule definition languages.
These rules originate from the BioPAX specification and the community best practice, 
are expressed in Java, as generic classes built mostly around the Paxtools API, 
and embraced by the aspect-oriented framework (AOP). One gets all the errors at once, 
in several categories and levels of importance, and the normalized BioPAX Level3 model (optional).
The BioPAX Validator can run online, from command line, or integrated into some application as a library.
It always converts BioPAX Level1, Level2 to Level3, before it checks the Level3 rules.
</blockquote>
</div>
</div>

<div class="row">
<p>
Please feel free to post your comments and suggestions at the 
<a href="http://www.biopax.org/wiki/index.php?title=BioPAXValidator">Validator Wiki</a> or 
<a href="http://sourceforge.net/p/biopax/_list/tickets">BioPAX issue tracker</a>.
</p>
</div>

<jsp:include page="footer.jsp"/>

</body>
</html>
