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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

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
	<title>BioPAX Validator: check</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">

<h2>Check BioPAX<b>*</b></h2>
<form id="validate" method="post" enctype="multipart/form-data" onsubmit="return validate();">
    <div class="form-row">
    	<input type="radio" id="switch" name="switch" checked="checked" onchange="switchInput();" value="fdfdfdf"/>
    	<label>Choose up to 25 BioPAX files:</label>
		<input id="file" type="file" name="file_0" /> 
		<!--we removed the accept="application/rdf+xml" attribute from this tag, because:  
		 - most BioPAX files have '.owl' extension (rather than recommended .rdf, .xml), and there is no owl mime type
		 - e.g., Google Chrome takes file extensions serious preventing files other than .xml from being selected in the file upload dialog
		-->
		<div id="files_list" ></div>
		<br/>
		<input type="radio" id="switch" name="switch" onchange="switchInput();"/>
		<label>Check a BioPAX OWL at the location:</label>
		<br/>
        <input id="url" class="input" type="text" name="url" size="80%" disabled="disabled"/>
        <br/>
        <label id="urlMsg" class="errorMsg">${error}</label>
    </div>
	<div class="form-row">	
		<input type="checkbox" id="autofix" name="autofix" value="true" onchange="updateValidatorOptions();"/>
		<label>Fix and Normalize (<a href="javascript:switchit('aboutFix')">What does it mean?..</a>)</label>
		
		<ul id="aboutFix">
			<li>some rules can also auto-fix, e.g., Xref's properties (using MIRIAM), 
			controlled vocabulary terms (using external ontologies), <em>displayName</em>, or remove duplicates, etc.;</li>
			<li>Then, the Normalizer replaces URIs, if possible, for such utility class objects
		as <em>EntityReference</em>, <em>ControlledVocabulary</em>, <em>BioSource</em>, <em>PublicationXref</em>, 
		with standard URIs (e.g., for a protein reference, it is like <em>http://identifiers.org/uniprot/Q06609</em>),
		and for the rest of Xrefs - with consistently auto-generated URIs.</li>
			<li>Normalizer does not re-check rules once again, and so errors are still reported with the reference to the original data (URIs)
			(although Validator does not re-check, users can still download and re-submit the modified BioPAX);</li>
			<li>"dangling" <em>UtilityClass</em> individuals will be removed (i.e., if there were no Entity class BioPAX elements at all, the result will be empty model, sorry);</li>
			<li>not all problems can be fixed automatically; some issues are consequence of others; even new errors may be introduced (see below);</li>
			<li>it cannot reliably fix such issues as: syntax errors, a <em>UnificationXref</em> shared by different objects, 
			no unification xrefs attached to important utility objects, such as non-generic EntityReference, etc., 
			- but you get a rough idea about what unsupervised tools might infer from this data and whether it is the knowledge one really wanted to share);</li>
			<li>normalization can be the first step in BioPAX data integration pipeline, which makes next steps easier.</li>
		</ul>
		<br/>
		
		<div id="normalizerOptions">
		<br/>
		Options: 
		<ul title="Options">
			<li><form:checkbox path="normalizer.fixDisplayName"/>&nbsp;<label>fix property: <em>displayName</em> (from names)</label></li>
			<li><form:checkbox path="normalizer.inferPropertyOrganism"/>&nbsp;<label>infer property: <em>organism</em></label></li>
			<li><form:checkbox path="normalizer.inferPropertyDataSource"/>&nbsp;<label>infer property: <em>dataSource</em></label></li>
			<li><em>xml:base</em> for generated URIs:<form:input path="normalizer.xmlBase"/><label>&nbsp;(leave empty to use a value from the BioPAX RDF/XML header)</label></li>
		</ul>
		</div>
		<br/>
		
		<div class="form-row">
		<label>Validation Profile:</label><br/>
		<select name="profile">
			<option label="Default (Best Practice)" value="" selected="selected">Default (Best Practice)</option>
			<option label="Alternative (Less Strict)" value="notstrict">Alternative (Less Strict)</option>
		</select>
		<br/><br/>		
		<label>Error Case Filter (Level):</label><br/>
		<select name="filter">
			<option label="Warnings and Errors (default)" value="WARNING" selected="selected">Warnings and Errors (default)</option>
			<option label="Only Errors" value="ERROR">Only Errors</option>
<!-- 			<option label="No Problems" value="IGNORE">No Problems</option> -->
		</select>
		<br/><br/>
		<label>Stop and Return When:</label><br/>
		<select name="maxErrors">
			<option label="All checked (default)"  value="0" selected="selected">All checked (default)</option>
			<option label="An ERROR found and not fixed (fail-fast)" value="1">An ERROR found and not fixed (fail-fast)</option>
			<option label="10 (unfixed) ERROR cases found" value="10">10 (unfixed) ERROR cases found</option>
			<option label="25 (unfixed) ERROR cases found" value="25">25 (unfixed) ERROR cases found</option>
		</select>
		</div>
		<br/>
	</div>
		<div class="form-row">
		Report as:
		<br/>
		<input type="radio" name="retDesired" value="html" id="retHtml" checked="checked"/>
		<label>HTML</label>
		<br/>
		<input type="radio" name="retDesired" value="xml"/>
		<label>XML (<a href="<c:url value='/ws.html'/>">unmarshalable</a>)</label>
		<br/>
		<input type="radio" name="retDesired" value="owl" id="retOwl" disabled="disabled"/>
		<label>BioPAX (if modified)</label>
	</div>
	<div class="form-buttons">
        <div class="button"><input name="submit" type="submit" value="Submit"/></div>
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

<script type="text/javascript" src="scripts/multifile_compressed.js"></script>
<script type="text/javascript">
  var multi_selector = new MultiSelector( document.getElementById( 'files_list' ), 25);
  multi_selector.addElement( document.getElementById( 'file' ) );
</script>

</body>
</html>
