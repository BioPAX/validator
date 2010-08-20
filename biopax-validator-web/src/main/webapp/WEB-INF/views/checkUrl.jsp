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
	<title>Validate from URL</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">


<h1>Specify a BioPAX resource URL</h1>
<form method="post">
    <div class="form-row">
        <input class="input" type="text" name="url"/>
    </div>       
    <div class="form-row">
		<br/>
		<input type="radio" name="retDesired" value="html" checked="checked"/>
		<label>get HTML</label>
		<br/>
		<input type="radio" name="retDesired" value="xml"/>
		<label>get XML</label>
		<br/>
	</div>
	<div class="form-buttons">
        <div class="button"><input name="submit" type="submit" value="Validate" /></div>
	</div>   
</form>
	
	
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

