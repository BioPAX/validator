<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<h2>Actions:</h2>
<ul id="nav" title="Select Action">
<li><a href="<c:url value="/checkFile.html"/>">Check Files</a></li>
<li><a href="<c:url value="/checkUrl.html"/>">Check (or Fix!) from URL</a></li>
<security:authorize ifAnyGranted="ROLE_USER">
<li><a href="<c:url value='/j_spring_security_logout' />">Logout: <security:authentication property="principal.username"/></a></li>
</security:authorize>
</ul>