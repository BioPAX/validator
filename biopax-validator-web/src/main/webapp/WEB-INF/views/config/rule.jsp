<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>Validation Rule</h1>

<c:if test="${not empty statusMessageKey}">
    <p><fmt:message key="${statusMessageKey}"/></p>
</c:if>

<form:form commandName="rule" method="post">
    <fieldset>
        <div class="form-row">
            <label for="Name">Name: </label><br/>
            <form:input path="name" size="40" readonly="true"/>
            <br/>
        </div>       
        <div class="form-row">
            <label for="Tip">Description: </label><br/>
			<form:textarea rows="3" cols="80" path="tip" readonly="true"/>
			<br/>
        </div>
        <div class="form-row">
            <label for="Behavior">Behavior: </label>
            <form:select path="behavior" items="${behaviors}"/>
        </div>
        <div class="form-buttons">
            <div class="button"><input name="submit" type="submit" value="update" /></div>
        </div>
    </fieldset>
</form:form>