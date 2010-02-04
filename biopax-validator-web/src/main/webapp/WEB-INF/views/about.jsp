<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<h1>About BioPAX Validator</h1>
<h2>Welcome!</h2>
<div style="width: 90%">
BioPAX has become an important standard for communicating the knowledge about biochemical processes. 
But errors that arise from data transformation, OWL "Open World" semantics, 
and the extensive use of external references can be a real obstacle and a pain. 
To address this problem, the Validator has both syntactic and semantic rules 
together with the cross-cutting error reporting framework, 
and it also makes use of such magic components as: Paxtools (BioPAX API), Ontology Lookup Service 
(helps with controlled vocabularies), and MIRIAM database (to check external references).
</div>
<div style="width: 90%">
So, the BioPAX rules were derived both from the OWL 
specification and the community best practices. 
They are generic Java classes based on the Paxtools in-memory BioPAX model, 
and more rules can be created and tuned into the application later. They can check across several BioPAX
entities and can be nested or overlap in their subjects, which might take more care to implement. There are 
both "fail-fast" and "post-model" validation modes. However, in most cases (e.g., when one checks an OWL file), 
the former is not required, so the fail-fast mode will come to the scene in the future software that 
will allow interactive model assembling and merging and use the BioPAX Validator API.
</div>
&nbsp;
<div>
Please feel free to post your comments, suggestions, and issues at the 
<a href="http://sourceforge.net/apps/mediawiki/biopax/index.php?title=BioPAXValidator">Validator Wiki</a> and 
<a href="http://sf.net/tracker/?group_id=85345">BioPAX issue tracker</a>.
</div>
&nbsp;
<!-- 
<div>
<c:url var="goJavadoc" value="/doc/index.html" />
<a href='<c:out value="${goJavadoc}"/>'>BioPAX Validator API</a>
</div>
-->

<h2>Links</h2>
<ul>
<li><a href="http://www.biopax.org">BioPAX</a></li>
<li><a href="http://sourceforge.net/projects/biopax">BioPAX (and Validator) Development</a></li>
<li><a href="http://www.pathwaycommons.org">Pathway Commons</a></li>
<li><a href="http://www.baderlab.org">Gary Bader Lab</a> (University of Toronto, TDCCBR)</li>
<li><a href="http://cbio.mskcc.org">cBio</a> (MSKCC)</li>
<li><a href="http://www.ebi.ac.uk/miriam/">MIRIAM</a></li>
<li><a href="http://www.ebi.ac.uk/ontology-lookup/">OLS</a></li>
</ul>