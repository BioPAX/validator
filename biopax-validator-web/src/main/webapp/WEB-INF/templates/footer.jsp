<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<div style="float: left">
<a href="<c:url value="/j_spring_security_logout" />">Logout</a> 
<security:authentication property="principal.username"/>
&nbsp;
<a href="<c:url value="/login.html" />">Login</a>
</div>
<div style="float: left; padding-left: 4em">
<a href="http://sf.net/tracker/?group_id=85345">Report Issue</a>
</div>
<div style="float: right">
<a href="http://www.gnu.org/licenses/lgpl-3.0.txt">
<img src="<c:url value="/images/lgplv3-88x31.png" />" width="88" height="31"/>
</a>
</div>
<div style="float: right">
Uses 
<a href="http://www.ebi.ac.uk/ontology-lookup">
<img src="<c:url value="/images/ols-logo-small.jpg" />" width="88" height="31"/>
</a>
</div>
<div style="float: right; padding-right: 2em">
BioPAX Validator V2.0 @Copyright 2010 UofT &amp; MSKCC
</div>
<br/>
