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
<div>
To upload and check BioPAX files, use a multi-part HTTP POST request 
to <a href="<c:url value='/checkFile.html'/>">this page</a>.
The server accepts up to 25 OWL files or 25Mb altogether (default settings).
</div>
<br/>
<div>
To validate either from a URL resource or using Pathway Commons ID, 
set the parameter <em>&quot;url&quot;</em> and POST the query to 
<a href="<c:url value='/checkUrl.html'/>">another page</a>
</div>
<br/>
<div>
Optionally, you may want to use the parameter <em>'retDesired'</em> (results format). Values are: "html" (default) or "xml".
</div>
<br/>
<div>
An example client application can be downloaded from <em>/paxtools</em> folder at: 
<a href="http://sourceforge.net/projects/biopax/files/">BioPAX Project Files</a>
(it connects to the http://www.biopax.org/biopax-validator/); 
or - <a href="http://biopax.hg.sourceforge.net/hgweb/biopax/paxtools/file/default/validator-client">browse sources here</a>.
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