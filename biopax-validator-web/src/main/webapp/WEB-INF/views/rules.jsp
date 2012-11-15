<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
	<title>Validation Rules</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">

<h2>Loaded BioPAX Rules</h2>
<table border title="BioPAX Rules">
    <tr>
        <th colspan="2">BioPAX Validation Rule</th>
        <th colspan="2">Behavior (reports as)</th>
    </tr>
    <tr>
        <th>Name</th>
        <th>Description</th>
        <th>Default Profile</th>
        <th>Not Strict Profile</th>
    </tr>
  <c:forEach var="rule" items="${rules}">
    <tr title="Name&Behavior">
        <td  title="${rule.tip}">${rule.name}</td>
        <td style="font-size: small">${rule.tip}</td>
        <td style="font-size: small; font-style: italic">${rule.stdProfile}</td>
		<td style="font-size: small; font-style: italic">${rule.altProfile}</td>
    </tr>
  </c:forEach>
</table>
	
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