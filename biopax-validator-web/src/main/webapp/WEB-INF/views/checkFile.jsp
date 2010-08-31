<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>

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
	<title>Check Files</title>
</head>
<body>
<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">


<h2>Choose up to Ten BioPAX Files*:</h2>
<form enctype="multipart/form-data" method="post">
	<div class="form-row">
		<input id="file" type="file" name="file_1" accept="application/rdf+xml"/>
	</div>
	<div id="files_list" ></div>
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
        <div class="button">
			<input type="submit" name="upload" value="Upload & Validate" />
		</div>
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
