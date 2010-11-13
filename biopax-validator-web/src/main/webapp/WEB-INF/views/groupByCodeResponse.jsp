<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page language="java" contentType="text/html; charset=UTF-8"%>

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
	<li style="text-decoration: underline" title="Click to see more detail">
		Resource:&nbsp;${result.description}&nbsp;
		<a href="javascript:switchit('result${rstatus.index}')">${result.summary}</a>
	</li>
	<c:forEach var="comment" items="${result.comment}">
		<li>${comment}</li>
	</c:forEach>
	<li>Auto-Fix = ${result.fix}</li>
	<li>Normalize = ${result.normalize}</li>
	<li>Errors and warnings (not fixed): ${result.totalProblemsFound}</li>
	<c:if test="${result.fix || result.normalize}">
	  	<li><a href="javascript:switchit('result${rstatus.index}owl')">Generated BioPAX OWL</a>&nbsp;
	  	("escaped" for HTML; if you do plan to process the data, better go back and choose either "BioPAX only" or "XML" as return format.)</li>
		<ul id="result${rstatus.index}owl" style="display: none">
			<li><div>${result.modelSerializedHtmlEscaped}</div></li>
		</ul>
	</c:if>
	<ul id="result${rstatus.index}" style="display: none">
	  <c:forEach var="errorType" items="${result.error}" varStatus="estatus">
		<li title="Click for error cases">
			<a href="javascript:switchit('result${rstatus.index}type${estatus.index}')">${errorType.type}</a>
			&nbsp;[<b>code: <em>${errorType.code}</em>; cases left: <em>${errorType.totalErrorCases}</em></b>]
			&nbsp;${errorType.message}
		</li>
		<ul id="result${rstatus.index}type${estatus.index}" style="display: none">
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
