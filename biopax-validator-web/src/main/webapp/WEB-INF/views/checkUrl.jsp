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
	<script type="text/javascript">
	  function switchInput() {
		  var f = document.getElementById('file');
		  var u = document.getElementById('url');
		  var cb = document.getElementById('switch');
		  if(cb.checked == true) {
			f.disabled = false;
			u.disabled = true;
			u.value = null;
			document.getElementById('urlMsg').innerHTML = null;
		  } else {
			f.disabled = true;
			f.value = null;
			u.disabled = false;
		  }
	  };

	  function isUrl(s) {
			var regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
			return regexp.test(s);
	  };

	  function validate() {
		  var u = document.getElementById('url');
		  if(!u.disabled && !isUrl(u.value)) {
			  document.getElementById('urlMsg').innerHTML = 'Malformed URL!';
			  return false;
		  }
	  };
	  
	</script>
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
		<input id="file" type="file" name="file" accept="application/rdf+xml"/>
		<br/>
		<input type="radio" id="switch" name="switch" onchange="switchInput();"/>
		<label>Check a BioPAX OWL at the location:</label>
		<br/>
        <input id="url" class="input" type="text" name="url" size="80%" disabled="disabled"/>
        <br/>
        <label style="color: red;" id="urlMsg">${error}</label>
    </div>
	<div class="form-row" style="padding-top: 2em;">
		Options:<br/>
		<input type="checkbox" name="autofix" value="true"/>
		<label>Auto-Fix! (<b>experimental:</b> some rules can, e.g., fix db/id, set 'displayName', remove duplicates, etc.)</label>
		<br/>
		<input type="checkbox" name="normalize" value="true"/>
		<label>Normalize! (<b>experimental:</b> where it is possible and makes sense, replaces RDFIDs of entity references, CVs, bioSource 
		with Miriam's standard URNs; also generates new IDs like <em>urn:biopax:*Xref:&lt;db&gt;_&lt;id&gt;_&lt;ver&gt;</em> for Xrefs; 
		this eases BioPAX data integration, sharing, and application of semantic web technologies)</label>
		<br/>
		<br/>
		<input type="checkbox" name="upgrade" value="true" disabled="disabled"/>
		<label><em>Upgrade (to Level3) first! (<b>coming soon</b>; currently, with a few exceptions, only syntax errors are checked in L2 data)</em></label>
		<br/>
		<input type="checkbox" name="convert" value="true" disabled="disabled"/>
		<label><em>PSI-MI to BioPAX (<b>may be useful...</b>)</em></label>
		<br/>
	</div>
		<div class="form-row" style="padding-top: 2em;">
		Report as:
		<br/>
		<input type="radio" name="retDesired" value="html" checked="checked"/>
		<label>HTML</label>
		<br/>
		<input type="radio" name="retDesired" value="xml"/>
		<label>XML (<a href="http://biopax.hg.sf.net/hgweb/biopax/validator/file/default/biopax-validator-core/src/main/java/org/biopax/validator/result/">unmarshalable</a>)</label>
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

</body>
</html>

