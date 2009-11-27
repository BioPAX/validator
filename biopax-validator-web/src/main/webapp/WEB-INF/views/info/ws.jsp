<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<h1>BioPAX Validator as Web Service</h1>
<br/>
<c:url var="goFile" value="/validator/checkFile.html"/>
<c:url var="goUrl" value="/validator/checkUrl.html" />
<div>
The same web pages may serve not only user but also software clients.
</div>
<br/>
<div>
To upload and validate BioPAX files, 
send a multi-part HTTP POST query 
to <a href='<c:out value="${goFile}"/>'>this page</a>
<br/>
By default the server accepts up to fifty 
OWL files and 50Mb altogether, which is configurable.
</div>
<br/>
<div>
To validate either from a URL resource or using Pathway Commons ID,<br/> 
set the parameter <em>&quot;url&quot;</em> and send a POST query, e.g., to 
<a href='<c:out value="${goUrl}"/>'>another page</a>
</div>
<br/>
<div>
Optionally, set the parameter  <em>&quot;retDesired&quot;</em> value "html" (default) or "xml".
</div>
<br/>
<div>
Example BioPAX validator client can be found in the <em>/validator</em> folder at: <br/>
<a href="http://sourceforge.net/projects/biopax/files/">BioPAX Project Files</a>
(it connects to the http://www.biopax.org/biopax-validator/)<br/>
or in the BioPAX CVS.
</div>
