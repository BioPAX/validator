<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<div id="top">
<h2><a href="http://www.biopax.org">BioPAX</a> Validator</h2>
<div id="menu">
<ul>
<li><a href="<c:url value='/'/>" class="current">home</a></li>
<li><a href="<c:url value='/ws.html' />">webservice</a></li>
<li><a href="<c:url value='/admin.html' />">settings</a></li>
<li><a href="http://sf.net/tracker/?group_id=85345">report issue</a></li>
<security:authorize ifAnyGranted="ROLE_USER">
<li><a href="<c:url value='/j_spring_security_logout' />">logout</a><security:authentication property="principal.username"/></li>
</security:authorize>
</ul>
</div>
</div>
