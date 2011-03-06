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
BioPAX is an important standard for communicating the knowledge about biochemical processes.  
In the course of data exchange, errors that occur in data modeling, 
curation, or transformation can be big obstacle. The BioPAX Validator is being developed 
to help improve the BioPAX data quality, which also makes pathway knowledge interpretation 
and integration possible in a greater level of detail.
It checks the BioPAX models for dozens of custom rules, some of which cannot be expressed 
(reasonably, at the moment, or at all) in OWL or related rule definition languages.
<p/>
The validator runs syntactic and advanced semantic validation rules that came from the 
BioPAX specification and community best practices, expressed in plain Java, and embraced by the 
cross-cutting error reporting framework. The rules are, basically, generic Java classes 
making use of Paxtools API. They can also check across several BioPAX entities, check entire model, 
can be nested, or overlap in their subjects. A BioPAX element can be checked in "real-time" 
(on property change) or "post-model" (after the model is complete) modes. 
However, in most cases such as when one checks files, the former is not required; but 
the "real-time" mode would come to play in an interactive BioPAX editor software that use this API.
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
