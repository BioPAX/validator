<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html>
<head>
	<title>BioPAX Validator: check</title>
	<jsp:include page="head.jsp"/>
</head>
<body>

<jsp:include page="header.jsp"/>

<h2>Check BioPAX<b>*</b></h2>

<div class="row">
<form id="validate" method="post" enctype="multipart/form-data" onsubmit="return validate();">
    <div class="form-group">
    	<input type="radio" id="switch" name="switch" checked="checked" onchange="switchInput();" value="fdfdfdf"/>
    	<label>Choose up to 25 BioPAX files:</label>
		<input id="file" type="file" name="file_0" /> 
		<!--we removed the accept="application/rdf+xml" attribute from this tag, because:  
		 - most BioPAX files have '.owl' extension (rather than recommended .rdf, .xml), and there is no owl mime type
		 - e.g., Google Chrome takes file extensions serious preventing files other than .xml from being selected in the file upload dialog
		-->
		<div id="files_list" ></div>
		<input type="radio" id="switch" name="switch" onchange="switchInput();"/>
		<label>Check a BioPAX OWL at the location:</label>
        <input id="url" class="input" type="text" name="url" size="80%" disabled="disabled"/>
        <label id="urlMsg" class="errorMsg">${error}</label>
    </div>
	<div class="form-group">	
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

		<div id="normalizerOptions">
		Options: 
		<ul title="Options">
			<li><form:checkbox path="normalizer.fixDisplayName"/>&nbsp;<label>fix property: <em>displayName</em> (from names)</label></li>
			<li><form:checkbox path="normalizer.inferPropertyOrganism"/>&nbsp;<label>infer property: <em>organism</em></label></li>
			<li><form:checkbox path="normalizer.inferPropertyDataSource"/>&nbsp;<label>infer property: <em>dataSource</em></label></li>
			<li><em>xml:base</em> for generated URIs:<form:input path="normalizer.xmlBase"/><label>&nbsp;(leave empty to use a value from the BioPAX RDF/XML header)</label></li>
		</ul>
		</div>
		
		<div class="form-group">
		<label>Validation Profile:</label><br/>
		<select name="profile">
			<option label="Default (Best Practice)" value="">Default (Best Practice)</option>
			<option label="Alternative (Less Strict)" value="notstrict" selected="selected">Alternative (Less Strict)</option>
		</select>

		<label>Error Case Filter (Level):</label><br/>
		<select name="filter">
			<option label="Warnings and Errors (default)" value="WARNING" selected="selected">Warnings and Errors (default)</option>
			<option label="Only Errors" value="ERROR">Only Errors</option>
<!-- 			<option label="No Problems" value="IGNORE">No Problems</option> -->
		</select>

		<label>Stop and Return When:</label><br/>
		<select name="maxErrors">
			<option label="All checked (default)"  value="0" selected="selected">All checked (default)</option>
			<option label="An ERROR found and not fixed (fail-fast)" value="1">An ERROR found and not fixed (fail-fast)</option>
			<option label="10 (unfixed) ERROR cases found" value="10">10 (unfixed) ERROR cases found</option>
			<option label="25 (unfixed) ERROR cases found" value="25">25 (unfixed) ERROR cases found</option>
		</select>
		</div>
	</div>
		<div class="form-group">
		Report as:
		<input type="radio" name="retDesired" value="html" id="retHtml" checked="checked"/>
		<label>HTML</label>
		<input type="radio" name="retDesired" value="xml"/>
		<label>XML (<a href="<c:url value='/ws.html'/>">unmarshalable</a>)</label>
		<input type="radio" name="retDesired" value="owl" id="retOwl" disabled="disabled"/>
		<label>BioPAX (if modified)</label>
	</div>
	<div class="form-buttons">
        <div class="button"><input name="submit" type="submit" value="Submit"/></div>
	</div>   
</form>
</div>
 
<jsp:include page="footer.jsp"/>

<script type="text/javascript" src="scripts/multifile_compressed.js"></script>
<script type="text/javascript">
  var multi_selector = new MultiSelector( document.getElementById( 'files_list' ), 25);
  multi_selector.addElement( document.getElementById( 'file' ) );
</script>

</body>
</html>
