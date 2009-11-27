<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h1>Available BioPAX Rules</h1>

<table border title="BioPAX Rules">
    <tr>
        <th>Name</th>
        <th>Behavior</th>
    </tr>
<c:forEach var="rule" items="${rules}">
    <tr title="Name&Behavior">
        <c:url var="editUrl" value="/config/rule.html">
            <c:param name="name" value="${rule.name}" />
        </c:url>
        <td  title="${rule.tip}">
            <a href='<c:out value="${editUrl}"/>'>${rule.name}</a>
        </td>
        <td style="font-size: small; font-style: italic">${rule.behavior}</td>
    </tr>
</c:forEach>
</table>