<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<!DOCTYPE html>
<html>
<head>
	<title>BioPAX Validator: other</title>
	<jsp:include page="head.jsp"/>
</head>
<body>

<jsp:include page="header.jsp"/>

<h2>Additional Configuration</h2>

<div class="jumbotron">
<h3>For XrefRule, XrefSynonymDbRule rules</h3>
<p>
Special groups of synonyms (or typos) for known databases 
can be configured via a Spring XML file, and later - auto-completed
by standard names from MIRIAM resource and PSI-MI ("database citation"). 
</p>
</div>
<div class="row">
 <ol>
  <c:forEach var="g" items="${extraDbSynonyms}">
	<li>group:
	 <ol>
	  <c:forEach var="db" items="${g}">
	    <li>${db}</li>
      </c:forEach>
     </ol>
    </li>
    <br />
  </c:forEach>
 </ol>
</div>

<jsp:include page="footer.jsp"/>

</body>
</html>