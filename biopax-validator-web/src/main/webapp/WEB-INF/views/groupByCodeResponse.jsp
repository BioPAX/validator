<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
	<title>Validation Results</title>
	<jsp:include page="head.jsp"/>
	<style type="text/css">.hidden { display: none; }</style>
</head>
<body>

<jsp:include page="header.jsp"/>

<h2>Validation Results</h2>
<p><label class="errorMsg">${error}</label></p>

<c:forEach var="result" items="${response.validationResult}" varStatus="rstatus">

  <div class="row">
  <h3>Model: ${result.description}</h3> 
  <ul class="list-inline">
	<li>
	  <c:forEach var="comment" items="${result.comment}">
		${comment}; 
	  </c:forEach>
	</li>
  </ul>	
  <table class="table table-bordered">
  	<thead>
  		<tr>
  			<th>Problems</th><th>Cases</th><th>Not fixed</th>
  			<th><c:choose><c:when test="${result.maxErrors > 0}">Max Errors (not fixed)</c:when>
				<c:otherwise>Not fixed ERRORs</c:otherwise></c:choose></th>
			<th>Profile</th><th>Auto-fix</th>
  		</tr>
  	</thead>
  	<tbody>
  		<tr>
  			<td>${result.summary}</td>
  			<td><span class="badge">${result.totalProblemsFound}</span></td>
  			<td><span class="badge">${result.notFixedProblems}</span></td>
  			<td><c:choose><c:when test="${result.maxErrors > 0}">${result.maxErrors}</c:when>
				<c:otherwise><span class="badge badge-error">${result.notFixedErrors}</span></c:otherwise>
				</c:choose>
			</td>
			<td>
  				<c:choose>
				<c:when test="${result.profile != null}">${result.profile}</c:when>
				<c:otherwise>default</c:otherwise>
	  			</c:choose>
  			</td>
  			<td>${result.fix}</td>
  		</tr>
  	</tbody>
  </table>
	<h4>Issues</h4>
	<ul>
	  <c:forEach var="errorType" items="${result.error}" varStatus="estatus">
		<li>
			<h4><a class="hider" hide-id="result${rstatus.index}type${estatus.index}" href="#">
			${errorType.type}: <code>${errorType.code}</code></a>
			, category: <em>${errorType.category}</em>, 
			cases: <span class="badge"><em>${errorType.totalCases}</em></span>,  
			<c:choose>
				<c:when test="${errorType.notFixedCases > 0}">
				not fixed: <span class="badge ${errorType.type}"><em>${errorType.notFixedCases}</em></span>
				</c:when>
				<c:otherwise>
				all fixed!
				</c:otherwise>
	  		</c:choose>
			</h4>
			${errorType.message}
		</li>
		<ul class="hidden" id="result${rstatus.index}type${estatus.index}">
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
	
	<c:if test="${result.fix}">
	  	<h4>Modified BioPAX model</h4>
	  	<a href="#" class="hider" hide-id="owl${rstatus.index}">Show/Hide</a>
		<div class="hidden" id="owl${rstatus.index}"><code>${result.modelDataHtmlEscaped}</code></div>
	</c:if>	
	
	</div>
	<hr/>
</c:forEach>

<jsp:include page="footer.jsp"/>

<script>
$(function() {       
    $('.hider').on('click', function(){
        var $hider = $(this);
        var hideeid = $hider.attr('hide-id');
        var $hidee = $('#' + hideeid);
        $hidee.toggleClass('hidden');
        return false;
    });    
});
</script>

</body>
</html>
