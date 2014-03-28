<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
<head>
	<title>BioPAX Validator: rules</title>
	<jsp:include page="head.jsp"/>
</head>
<body>

<jsp:include page="header.jsp"/>

<h2>BioPAX Semantic Rules (as Java classes)</h2>

<dl>
  <c:forEach var="rule" items="${rules}">
  	<dt>name:&nbsp;<dfn>${rule.name}</dfn></dt>
    <dd><p>description:&nbsp;<dfn>${rule.tip}</dfn><br/>
        profiles:&nbsp;<dfn>${rule.stdProfile} ("default"), ${rule.altProfile} ("notstrict")</dfn></p>
    </dd>   
  </c:forEach>
</dl>
	
<jsp:include page="footer.jsp"/>

</body>
</html>