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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

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
	<title>BioPAX Validator: errors</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">

<h2>Validation Error Classes</h2>
<p>
Every class of error &lt;code&gt; is defined in the error-codes.properties as follows: 
</p>
<ul>
<li><b>&lt;code&gt;.default</b>=<em>a common message to show for all such cases</em></li>
<li><b>&lt;code&gt;</b>=<em>a specific message template with optional parameters, e.g.: property={0}, value={1}. 
(Note: the id of the associated with the error case object will be added to the error message automatically, in all cases!)</em></li>
<li><b>&lt;code&gt;.category</b>=<em>one of: syntax, specification, recommendation, information</em></li>
</ul>
<p/>
<h3>
Following error classes (codes) can be reported by the BioPAX rules:
</h3>
<dl>
  <c:forEach var="err" items="${errorTypes}">
    <dt>code:&nbsp;<dfn>${err.code}</dfn>, category:&nbsp;<dfn>${err.category}</dfn></dt>
    <dd><p>common message:&nbsp;<dfn>${err.defaultMsg}</dfn><br/>
        case-specific template:&nbsp;<code>${err.caseMsgTemplate}</code></p>
	</dd>
  </c:forEach>
</dl>

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