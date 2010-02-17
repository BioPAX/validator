<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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

<h1>Validation Results</h1>
<ul>
<c:forEach var="result" items="${response.validationResult}" varStatus="rstatus">
	<li><u>Resource:&nbsp;${result.description}&nbsp;<a href="javascript:switchit('result${rstatus.index}')">${result.summary}</a></u></li>
	<c:forEach var="comment" items="${result.comment}">
		<li>${comment}</li>
	</c:forEach>
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