<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<h1>Settings</h1>
<br/>
<div>
<security:authorize ifNotGranted="ROLE_ADMIN">
Everyone can see but only Admin modify the following
</security:authorize>
<security:authorize ifAnyGranted="ROLE_ADMIN">
All changes immediately apply for all users.
</security:authorize>
</div>
<br/>
<div>
<c:url var="errUrl" value="/config/errors.html" />
<a href='<c:out value="${errUrl}"/>'>General Configuration</a>
</div>
<br/>
<div>
<c:url var="rulesUrl" value="/config/rules.html" />
<a href='<c:out value="${rulesUrl}"/>'>Validation Rules</a>
<br/>
</div>