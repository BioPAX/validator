<%--
  #%L
  BioPAX Validator Web Application
  %%
  Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as 
  published by the Free Software Foundation, either version 3 of the 
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Lesser Public License for more details.
  
  You should have received a copy of the GNU General Lesser Public 
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/lgpl-3.0.html>.
  #L%
  --%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page language="java" contentType="text/html; charset=UTF-8"%>

<!DOCTYPE html>
<html>
<head>
   <meta charset="utf-8" />
	<meta name="author" content="BioPAX" />
	<meta name="description" content="BioPAX Validator" />
	<meta name="keywords" content="BioPAX, Validation, Validator, Rule, OWL, Exchange" />
	<link rel="stylesheet" type="text/css" href="styles/style.css" media="screen" />
	<link rel="shortcut icon" href="images/favicon.ico" />
	<script type="text/javascript" src="scripts/rel.js"></script>
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
<title>Validation Results</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">

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

	
	
    </div>
    <div id="right">
      <jsp:include page="/templates/menu.jsp"/>
      <jsp:include page="/templates/box.jsp"/>
    </div>
    <div id="clear"></div>
  </div>

  <jsp:include page="/templates/footer.jsp"/>
</div>

</body>
</html>
