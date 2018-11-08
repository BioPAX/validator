<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
<head>
	<title>BioPAX Validator: rules</title>
	<jsp:include page="head.jsp"/>
</head>
<body>

<jsp:include page="header.jsp"/>

<h2>BioPAX Semantic Rules</h2>

<div class="row">
<h4>Loaded validation rules and corresponding
level (error/warning/ignore) in each mode (profile).</h4>
<table class="table table-striped table-bordered">
<thead>
<tr><th colspan="2"></th><th colspan="2">Profile and level</th></tr>
<tr>
<th>Rule (class name)</th><th>Description</th><th>"default"</th><th>"notstrict"</th>
</tr>
</thead>
<tbody>
  <c:forEach var="rule" items="${rules}">
  	<tr>
  	<td><dfn>${rule.name}</dfn></td>
    <td>${rule.tip}</td>
    <td>${rule.stdProfile}</td>
    <td>${rule.altProfile}</td>  
    </tr> 
  </c:forEach>
 </tbody>
</table>

</div>
	
<jsp:include page="footer.jsp"/>

</body>
</html>