<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>Errors Framework Settings</h1>

<c:url var="url" value="/config/errors.html"/>
<form:form action="${url}" commandName="utils">
<fieldset>
       <div class="form-row">
            <label for="enabled">Max. Errors</label>
            <form:input path="maxErrors" />
        </div>
        <div class="form-buttons">
            <div class="button"><input name="submit" type="submit" value="Update" /></div>
        </div>
</fieldset>
</form:form>