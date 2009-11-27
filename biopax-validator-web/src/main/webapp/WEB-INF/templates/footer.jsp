<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

Version 1.2b4 @Copyright 2009
&nbsp;<img src="<c:url value="/images/new.jpg" />" />
&nbsp;<a href="<c:url value="/j_spring_security_logout" />">Logout</a> <security:authentication property="principal.username"/>
&nbsp;<a href="<c:url value="/login.html" />">Login</a> 