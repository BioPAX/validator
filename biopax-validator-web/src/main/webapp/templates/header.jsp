<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<div id="top">
<h2><a href="http://www.biopax.org">BioPAX</a> Validator ${project.version}</h2>
<div id="menu">
<ul>
<li><a href="<c:url value='/'/>">Home</a></li>
<li><a href="<c:url value="/check.html"/>">Validate</a></li>
<li><a href="http://sf.net/tracker/?group_id=85345">Report</a></li>

<security:authorize ifAnyGranted="ROLE_USER">
<li><a href="<c:url value='/j_spring_security_logout' />">Logout: <security:authentication property="principal.username"/></a></li>
</security:authorize>

</ul>
</div>
</div>
