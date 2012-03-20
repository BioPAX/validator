<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
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

<h2>Check BioPAX<b>*</b></h2>
<form method="post" enctype="multipart/form-data" onsubmit="return validate();">
    <div class="form-row" style="padding-top: 1em;">
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
        <label style="color: red;" id="urlMsg">${error}</label>
    </div>
	<div class="form-row" style="padding-top: 2em;">
		Options (<a href="javascript:switchit('aboutOptions')">what if it's enabled?</a>):<br/>
		<ul id="aboutOptions" style="display: none;">
			<li>errors and warnings are still reported with the reference to the original data 
			(validator does not re-check, however, users can download and re-submit the modified BioPAX);</li>
			<li>"dangling" <em>UtilityClass</em> (and sub-classes) individuals will be removed;</li>
			<li>not all problems can be fixed automatically; some issues are consequence of others; 
			even new errors may be introduced (see below);</li>
			<li>it cannot reliably fix such issues as: syntax errors, a <em>UnificationXref</em> shared by different objects, 
			no unification xrefs attached to important utility objects, such as non-generic EntityReference, etc., 
			- but you get a rough idea about what unsupervised tools might infer 
			from this data and whether it is the knowledge one really wanted to share);</li>
			<li>normalization can be the first step in BioPAX data integration pipeline, which makes next steps easier.</li>
		</ul>
		
		<input type="checkbox" name="autofix" value="true"/>
		<label>Auto-Fix! (<b>experimental</b>): e.g., some rules can fix xref's <em>db/id</em> properties, 
		controlled vocabularies (use of external ontologies), fix <em>displayName</em>, remove duplicates, etc.</label>
		<br/>
		
		<input type="checkbox" id="normalize" name="normalize" value="true" onchange="switchNormalizerOptions();" />
		<label>Normalize! (<b>experimental)</b>: if possible, replaces URIs of <em>EntityReference</em>, 
		<em>ControlledVocabulary</em>, <em>BioSource</em>, <em>PublicationXref</em>, and <em>Provenance</em> 
		with MIRIAM standard URNs; e.g., for a protein reference, it can get a URN like <em>urn:miriam:uniprot:Q06609</em>;
		for other xrefs, - like <em>urn:biopax:UnificationXref:&lt;db&gt;_&lt;id&gt;_&lt;ver&gt;</em>.
		If required, data are auto-converted to BioPAX Level3 first.</label>

		<br/>
		 
		<ul id="normalizerOptions" style="display: none;">
			<li><form:checkbox path="options.fixDisplayName"/>&nbsp;<label>fix displayName (from names)</label></li>
			<li><form:checkbox path="options.inferPropertyOrganism"/>&nbsp;<label>infer property: organism</label></li>
			<li><form:checkbox path="options.inferPropertyDataSource"/>&nbsp;<label>infer property: dataSource</label></li>
			<li><form:checkbox path="options.generateRelatioshipToPathwayXrefs"/>&nbsp;<label>generate pathway relationship xrefs</label></li>
			<li><form:checkbox path="options.generateRelatioshipToInteractionXrefs"/>&nbsp;<label>generate interaction relationship xrefs</label></li>
			<li><form:checkbox path="options.generateRelatioshipToOrganismXrefs"/>&nbsp;<label>generate organism relationship xrefs</label></li>
		</ul>
		<br/>
		<div class="form-row" style="padding-top: 2em;">
		<label>Set the level (do the job, do not report everything)</label><br/>
		<select name="filter">
			<option label="Show Warnings and Errors" value="WARNING" selected="selected">Show Warnings and Errors</option>
			<option label="Show Errors Only" value="ERROR">Show Errors Only</option>
			<option label="Do not Show Me Problems :)" value="IGNORE">Do not Show Me Problems :)</option>
		</select>
		<br/>
		<label>Set the limit (of type:ERROR, not fixed cases)</label><br/>
		<select name="maxErrors">
			<option label="Unlimited" selected="selected">0</option>
			<option label="'Fail-fast' (after the first serious case)">1</option>
			<option label="Do up to 10 error cases">10</option>
		</select>
		</div>
		<br/>
	</div>
		<div class="form-row" style="padding-top: 2em;">
		Report as:
		<br/>
		<input type="radio" name="retDesired" value="html" checked="checked"/>
		<label>HTML</label>
		<br/>
		<input type="radio" name="retDesired" value="xml"/>
		<label>XML (<a href="<c:url value='/ws.html'/>">unmarshalable</a>)</label>
		<br/>
		<input type="radio" name="retDesired" value="owl"/>
		<label>Modified BioPAX (only)</label>
	</div>
	<div class="form-buttons" style="padding-top: 2em;">
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
