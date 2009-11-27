<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h1>Choose up to Ten BioPAX Files:</h1>
<div>
<form action="checkFile.html" enctype="multipart/form-data" method="post">
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
<script src="<c:url value="/scripts/multifile_compressed.js" />"> </script>
<script>
	var multi_selector = new MultiSelector( document.getElementById( 'files_list' ), 50);
	multi_selector.addElement( document.getElementById( 'file' ) );
</script>