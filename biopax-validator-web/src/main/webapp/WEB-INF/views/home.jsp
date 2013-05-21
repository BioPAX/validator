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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
	<title>BioPAX Validator: home</title>
</head>

<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">

<h2>Welcome</h2>
<p>
BioPAX is a standard for communicating the knowledge about biochemical processes.  
The BioPAX Validator is to help detect and fix syntax and non-trivial semantic issues 
introduced in the course of biological pathway data modeling, mapping, and exporting to BioPAX. 
High quality pathway knowledge is easier to analyze and merge at a much greater level of detail and with more generic, standard tools. 
The BioPAX Validator contains dozens of custom rules, some of which cannot be expressed in OWL or other rule definition languages.
These rules come from BioPAX Level3 specification and the community best practice, are expressed in plain Java and embraced by the 
validator's original cross-cutting error reporting framework. The rules are, basically, generic Java classes 
built around Paxtools API. The BioPAX Validator can run online, from command line, or integrated into other java applications as a library.
The Validator always converst BioPAX Level1, Level2 to Level3 internally, before it checks Level3 rules; and one can get the Level3 result back
along with errors and warnings.
</p>
<p>
Please feel free to post your comments, suggestions, and issues at the 
<a href="http://sourceforge.net/apps/mediawiki/biopax/index.php?title=BioPAXValidator">Validator Wiki</a> and 
<a href="http://sf.net/tracker/?group_id=85345">BioPAX issue tracker</a>.
</p>
	
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
