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
		<input type="checkbox" id="autofix" name="autofix" value="true" onchange="switchNormalizerOptions();"/>
		<label>Fix and Normalize (<a href="javascript:switchit('aboutFix')">What does it mean?..</a>)</label>
		
		<ul id="aboutFix" style="display: none;">
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
		
		<div id="normalizerOptions" style="display: none;">
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
		
		<div class="form-row" style="padding-top: 2em;">
		<label>Choose a validation profile</label><br/>
		<select name="profile">
			<option label="Default Profile (Best Practice)" value="" selected="selected">Default Profile (Best Practice)</option>
			<option label="Alternative Profile (Less Strict)" value="notstrict">Alternative Profile (Less Strict)</option>
		</select>
		<br/><br/>		
		<label>Set the level (do the job, do not report everything)</label><br/>
		<select name="filter">
			<option label="Show Warnings and Errors (default)" value="WARNING" selected="selected">Show Warnings and Errors (default)</option>
			<option label="Show Errors Only" value="ERROR">Show Errors Only</option>
			<option label="Do Not Show Any Problems" value="IGNORE">Do Not Show Any Problems</option>
		</select>
		<br/><br/>
		<label>Set the max limit of not fixed ERRORs (not warnings)</label><br/>
		<select name="maxErrors">
			<option label="Unlimited (default)"  value="0" selected="selected">Unlimited (default)</option>
			<option label="Fail-fast (after the first ERROR)" value="1">Fail-fast (after the first ERROR)</option>
			<option label="Up to 10 ERROR Cases" value="10">Up to 10 ERROR Cases</option>
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
