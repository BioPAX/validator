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

<h2>Validate BioPAX</h2>

<div class="row">
<form id="validate" method="post" enctype="multipart/form-data" onsubmit="return validate();">
    <div class="form-group">
    	<label>
    		<input type="radio" id="optionFiles" name="switch" checked="checked" 
    		onchange="switchInput();" value="fdfdfdf"/> BioPAX from Files (max. 25): 
    	</label>	
    	<div class="btn btn-default"> 	
    		<div id="files_list" ></div>
			<input id="file" type="file" />
		</div>
		
		<label>
			<input type="radio" id="optionUrl" name="switch" onchange="switchInput();"/>
			BioPAX from URL: 
        	<input id="url" class="input" type="text" name="url" size="80%" disabled="disabled"/>
        	<span id="urlMsg" class="errorMsg">${error}</span>
        </label>
    </div>
	<div class="form-group">	
		<input type="checkbox" id="autofix" name="autofix" value="true" onchange="updateValidatorOptions();"/>
		<label for="normalizerOptions">Fix and Normalize</label>
		<ul id="normalizerOptions" title="Options">
			<li><form:checkbox path="normalizer.fixDisplayName"/>&nbsp;<label>fix property: <em>displayName</em> (from names)</label></li>
			<li><em>xml:base</em> for generated URIs:<form:input path="normalizer.xmlBase"/><label>&nbsp;(leave empty to use a value from the BioPAX RDF/XML header)</label></li>
		</ul>
		<p><small>-auto-fix for xrefs, vocabularies, <em>displayName</em>, duplicate and dangling elements removal, etc.
		The Normalizer replaces URIs of canonical objects, such as <em>EntityReference</em>, <em>ControlledVocabulary</em>, <em>BioSource</em>, 
		<em>PublicationXref</em>, with standard URIs (e.g., <em>http://identifiers.org/uniprot/Q06609</em> - for a protein reference).
		Xrefs are consistently re-used, where possible, and being assigned with a generated URI. 
		Original URIs of pathways, physical entities, interactions are preserved. 
		Normalizer does not re-validate the modified model (one can validate again); errors will refer to original URIs.
		Not all problems can be fixed automatically, and some issues are consequence of others.
		Syntax errors, shared or completely missing unification xrefs usually cannot be fixed automatically.
		</small></p>
	</div>
	<div class="form-group col-xs-4">
		<label for="profile">Validation Profile:</label><br/>
		<select name="profile">
			<option label="Default (Best Practice)" value="">Default (Best Practice)</option>
			<option label="Alternative (Less Strict)" value="notstrict" selected="selected">Alternative (Less Strict)</option>
		</select>
	</div>
	<div class="form-group col-xs-4">
		<label for="filter">Error Case Filter (Level):</label><br/>
		<select name="filter">
			<option label="Warnings and Errors (default)" value="WARNING" selected="selected">Warnings and Errors (default)</option>
			<option label="Only Errors" value="ERROR">Only Errors</option>
		</select>
	</div>
	<div class="form-group col-xs-4">
		<label for="maxErrors">Stop after:</label><br/>
		<select name="maxErrors">
			<option label="all errors/warnings collected (default)"  value="0" selected="selected">all errors/warnings collected (default)</option>
			<option label="after one ERROR (fail fast)" value="1">after one ERROR (fail fast)</option>
			<option label="after 10 ERRORs (not fixed)" value="10">10 ERROR cases (not fixed)</option>
			<option label="after 25 ERRORs (not fixed)" value="25">25 ERROR cases (not fixed)</option>
		</select>
	</div>
	<div class="form-group">
		Report as:
		<input id="retHtml" type="radio" name="retDesired" value="html" id="retHtml" checked="checked"/>
		<label for="retHtml">HTML</label>
		<input id="retXml" type="radio" name="retDesired" value="xml"/>
		<label for="retXml">XML (<a href="<c:url value='ws.html'/>">unmarshalable</a>)</label>
		<input id="retOwl" type="radio" name="retDesired" value="owl" id="retOwl" disabled="disabled"/>
		<label for="retOwl">BioPAX (if modified)</label>
	</div>
	<div class="form-actions">
        <button class="btn btn-primary btn-large" name="submit" type="submit">Validate</button>
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
