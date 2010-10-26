<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
   <meta http-equiv="content-type" content="text/html;charset=utf-8" />
	<meta name="author" content="BioPAX" />
	<meta name="description" content="BioPAX Validator" />
	<meta name="keywords" content="BioPAX, Validation, Validator, Rule, OWL, Exchange" />
	<link rel="stylesheet" type="text/css" href="styles/style.css" media="screen" />
	<link rel="shortcut icon" href="images/favicon.ico" />
	<script type="text/javascript" src="scripts/rel.js"></script>
	<script type="text/javascript">
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
<ul>
<c:forEach var="result" items="${response.validationResult}" varStatus="rstatus">
	<li style="text-decoration: underline">Resource:&nbsp;${result.description}&nbsp;<a href="javascript:switchit('result${rstatus.index}')">${result.summary}</a></li>
	<c:forEach var="comment" items="${result.comment}">
		<li>${comment}</li>
	</c:forEach>
	<li>auto-fix: ${result.fix}</li>
	<li>normalize: ${result.normalize}</li>
	<c:if test="${result.fix || result.normalize}">
	  	<li><a href="javascript:switchit('result${rstatus.index}owl')">Generated OWL:</a></li>
		<ul id="result${rstatus.index}owl" style="display: none">
			<li><div>${result.fixedOwl}</div></li>
		</ul>
	</c:if>
	<ul id="result${rstatus.index}" style="display: none">
	  <c:forEach var="errorType" items="${result.error}" varStatus="estatus">
		<li><a href="javascript:switchit('result${rstatus.index}type${estatus.index}')">${errorType.type}</a>&nbsp;(<em>${errorType.code}</em>):&nbsp;${errorType.message}</li>
		<ul id="result${rstatus.index}type${estatus.index}" style="display: none">
		<c:forEach var="errorCase" items="${errorType.errorCase}">
			<li><b>${errorCase.object}</b>&nbsp;(rule: ${errorCase.reportedBy}):<div>${errorCase.message}</div></li>
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
