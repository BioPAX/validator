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

<h2>Welcome</h2>
<div style="width: 90%">
BioPAX is a standard for communicating the knowledge about biochemical processes.  
The BioPAX Validator is to help detect and fix syntax and non-trivial semantic issues 
introduced in the course of biological pathway data modeling, mapping, and exporting to BioPAX. 
High quality pathway knowledge is easier to analyze and merge at a much greater level of detail and with more generic, standard tools. 
The BioPAX Validator contains dozens of custom rules, some of which cannot be expressed in OWL or other rule definition languages.
These rules come from BioPAX Level3 specification and the community best practice, are expressed in plain Java and embraced by the 
validator's original cross-cutting error reporting framework. The rules are, basically, generic Java classes 
built around Paxtools API. The BioPAX Validator can run online, from command line, or integrated into other java applications as a library.
The Validator always converst BioPAX Level1, Level2 to Level3 internally, before it checks Level3 rules; and one can get the Level3 result back
along with errors and warnings.
</div>

	<h2>See Also:</h2>
	<div>
		<a href='<c:url value="/ws.html"/>'>About Webservice</a> (and paremeters)
	</div>
	<br/>		
	<div>
		<a href='<c:url value="/rules.html"/>'>Validation Rules</a> (description and configurations)
	</div>
	<br/>
	<div>
		<a href='<c:url value="/errorTypes.html"/>'>Validation Errors</a> (codes, categories, messages)
	</div>
	<br/>
	<div>
		<a href='<c:url value="/extraCfg.html"/>'>Extra Properties</a> (a couple of rules use XML configuration too)
	</div>
<br/>

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
