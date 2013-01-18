<%--
  #%L
  BioPAX Validator Web Application
  %%
  Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as 
  published by the Free Software Foundation, either version 3 of the 
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Lesser Public License for more details.
  
  You should have received a copy of the GNU General Lesser Public 
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/lgpl-3.0.html>.
  #L%
  --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h2>Actions:</h2>
<ul id="nav" title="Actions">
<li><a href="<c:url value="/check.html"/>">Validate,fix,normalize</a></li>

</ul>

<h2>Information:</h2>
<ul id="nav" title="Information">
<li><a href="<c:url value='/ws.html' />">About Webservice</a></li>
<li><a href='<c:url value="/rules.html"/>'>Validation Rules</a></li>
<li><a href='<c:url value="/errorTypes.html"/>'>Validation Errors</a></li>
<li><a href='<c:url value="/extraCfg.html"/>'>Extra Properties</a></li>
</ul>