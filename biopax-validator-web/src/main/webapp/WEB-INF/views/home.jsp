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
	<title>Validator's Welcome Page</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">

<h2>Welcome!</h2>
<div style="width: 90%">
BioPAX has become an important standard for communicating the knowledge about biochemical processes. 
But errors that arise from data transformation, OWL "Open World" semantics, 
and the extensive use of external references can be a real obstacle and a pain. 
To address this problem, the Validator has both syntactic and semantic rules 
together with the cross-cutting error reporting framework, 
and it also makes use of such magic components as: Paxtools (BioPAX API), Ontology Lookup Service 
(helps with controlled vocabularies), and MIRIAM database (to check external references).
</div>
<div style="width: 90%">
So, the BioPAX rules were derived both from the OWL 
specification and the community best practices. 
They are generic Java classes based on the Paxtools in-memory BioPAX model, 
and more rules can be created and tuned into the application later. They can check across several BioPAX
entities and can be nested or overlap in their subjects, which might take more care to implement. There are 
both "fail-fast" and "post-model" validation modes. However, in most cases (e.g., when one checks an OWL file), 
the former is not required, so the fail-fast mode will come to the scene in the future software that 
will allow interactive model assembling and merging and use the BioPAX Validator API.
</div>
&nbsp;
<div>
Please feel free to post your comments, suggestions, and issues at the 
<a href="http://sourceforge.net/apps/mediawiki/biopax/index.php?title=BioPAXValidator">Validator Wiki</a> and 
<a href="http://sf.net/tracker/?group_id=85345">BioPAX issue tracker</a>.
</div>
&nbsp;

 	
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
