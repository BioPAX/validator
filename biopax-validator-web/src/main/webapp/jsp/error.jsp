<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
	<title>Error Page</title>
	<jsp:include page="head.jsp"/>
</head>
<body>

<jsp:include page="header.jsp"/>

<h2>Error</h2>

<c:out value="${exception}" /> <c:out value="${exception.message}" />
<ul>
<c:forEach var="trace" items="${exception.stackTrace}">
	<li>${trace}</li>
</c:forEach>
</ul>	

<jsp:include page="footer.jsp"/>"

</body>
</html>
