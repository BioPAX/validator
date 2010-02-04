<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h1>How to Check BioPAX Data?</h1>
<div>
<br/>
<c:url var="goFile" value="/validator/checkFile.html" />
<a href='<c:out value="${goFile}"/>'>Upload Local Files</a>
<br/>
</div>
<div>
<br/>
<c:url var="goUrl" value="/validator/checkUrl.html" />
<a href='<c:out value="${goUrl}"/>'>Get from URL</a>
<br/>
</div>