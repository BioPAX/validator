<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" href="<c:url value="/styles/main.css"/>" type="text/css"/>
	<title>BioPAX Validator: Access Denied</title>
</head>

<body>
<div id="main_wrapper">

<h1>Access Denied</h1>

<p>Sorry, Access Denied</p>
<p>
<a href="<c:url value='/index.html' />">Return to Home Page</a> or 
<a href="<c:url value='/j_spring_security_logout' />">Logout</a>
</p>

</div>
</body>

</html>
