<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
	<title>Validation Results</title>
	<jsp:include page="head.jsp"/>
</head>
<body>

<jsp:include page="header.jsp"/>

<h2>Validation Results</h2>

<p><label class="errorMsg">${error}</label></p>
<ul id="validations">
<c:forEach var="result" items="${response.validationResult}" varStatus="rstatus">
	<li title="Click to see more detail">
		<a href="javascript:switchit('result${rstatus.index}')">Resource:&nbsp;${result.description}</a>;&nbsp;${result.summary}
	</li>
  <ul style="display: block;">
	<li>
	  <c:forEach var="comment" items="${result.comment}">
		${comment}&nbsp;
	  </c:forEach>
	</li>
	<li>
	  <c:choose>
		<c:when test="${result.profile != null}">profile: ${result.profile};&nbsp;</c:when>
		<c:otherwise>profile: default;&nbsp;</c:otherwise>
	  </c:choose>
	  auto-fix: ${result.fix}
	</li>
	<li>
	  errors/warnings: ${result.totalProblemsFound};&nbsp;- not fixed: ${result.notFixedProblems};&nbsp; 
	  <c:choose>
		<c:when test="${result.maxErrors > 0}">
			errors limit: ${result.maxErrors} (not fixed)
		</c:when>
		<c:otherwise>
			errors not fixed: ${result.notFixedErrors}
		</c:otherwise>
	  </c:choose>
	</li>
	
	<c:if test="${result.fix}">
	  	<li><a href="javascript:switchit('result${rstatus.index}owl')">Modified BioPAX</a>&nbsp;(HTML-escaped BioPAX RDF/XML)</li>
		<ul id="result${rstatus.index}owl" class="vOwl">
			<li><div>${result.modelDataHtmlEscaped}</div></li>
		</ul>
	</c:if>
  </ul>
	
	<ul id="result${rstatus.index}">
	  <c:forEach var="errorType" items="${result.error}" varStatus="estatus">
		<li title="Click to see the error cases">
			<a href="javascript:switchit('result${rstatus.index}type${estatus.index}')">
			${errorType.type}: <em>${errorType.code}</em>,&nbsp;category: <em>${errorType.category}</em>,
			&nbsp;cases: <em>${errorType.totalCases}</em>,&nbsp;
			<c:choose>
				<c:when test="${errorType.notFixedCases > 0}">
				not fixed: <em>${errorType.notFixedCases}</em>
				</c:when>
				<c:otherwise>
				all fixed!
				</c:otherwise>
	  		</c:choose>
			</a><br/>${errorType.message}
		</li>
		<ul id="result${rstatus.index}type${estatus.index}">
		<c:forEach var="errorCase" items="${errorType.errorCase}">
			<li>
				<c:if test="${errorCase.fixed}"><b>[FIXED!]</b>&nbsp;</c:if>
				object:<b>&nbsp;${errorCase.object}</b>
				<div>${errorCase.message}</div>(found by: <em>${errorCase.reportedBy}</em>)
			</li>
		</c:forEach>
		</ul>
	  </c:forEach>
	</ul>
	<br/>
</c:forEach>
</ul>

<jsp:include page="footer.jsp"/>

<!-- the script is here for the page to switch lists' view mode when used off-line too-->
<script type="text/javascript"> <!-- this function is here (not in a separate .js file) for off-line use -->
	function switchit(list) {
		var listElementStyle = document.getElementById(list).style;
		if (listElementStyle.display == "none") {
			listElementStyle.display = "block";
		} else {
			listElementStyle.display = "none";
		}
	}
</script>

</body>
</html>
