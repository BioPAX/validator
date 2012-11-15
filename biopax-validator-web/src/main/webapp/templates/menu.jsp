<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<h2>Actions:</h2>
<ul id="nav" title="Actions">
<li><a href="<c:url value="/check.html"/>">Validate,fix,normalize</a></li>

<%-- <security:authorize ifAnyGranted="ROLE_USER"> --%>
<%-- <li><a href="<c:url value='/j_spring_security_logout' />">Logout: <security:authentication property="principal.username"/></a></li> --%>
<%-- </security:authorize> --%>
</ul>

<h2>Information:</h2>
<ul id="nav" title="Information">
<li><a href="<c:url value='/ws.html' />">About Webservice</a></li>
<li><a href='<c:url value="/rules.html"/>'>Validation Rules</a></li>
<li><a href='<c:url value="/errorTypes.html"/>'>Validation Errors</a></li>
<li><a href='<c:url value="/extraCfg.html"/>'>Extra Properties</a></li>
</ul>