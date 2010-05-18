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
	<title>Access Denied</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">

<h2>Access Denied</h2>
<p>Sorry, Access Denied</p>
<p>
<a href="<c:url value='/' />">Return to Home Page</a> or 
<a href="<c:url value='/j_spring_security_logout' />">Logout</a>
</p>
	
     </div>
    <div id="right">
      <jsp:include page="/templates/menu.jsp"/>"
      <jsp:include page="/templates/box.jsp"/>"
    </div>
    <div id="clear"></div>
  </div>
  <jsp:include page="/templates/footer.jsp"/>"
</div>

</body>
</html>