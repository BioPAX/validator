<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h1>Specify a BioPAX URL or PathwayCommons ID</h1>
<div>
<form action="checkUrl.html" method="post">
    <div class="form-row">
        <input class="input" type="text" name="url"/>
    </div>       
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
        <div class="button"><input name="submit" type="submit" value="Validate" /></div>
	</div>   
</form>
</div>
