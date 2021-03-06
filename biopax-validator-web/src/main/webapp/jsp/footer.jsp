<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!-- end of id="content" class="container" -->
</div>

<footer class="footer">
	<div id="footer_navbar" class="navbar navbar-default navbar-fixed-bottom">
          <div class="container">	
			  <p class="navbar-text navbar-left">
				<em>The Web application is not for too many/large BioPAX files.
					For batch validation, consider using the console version of the validator.</em><br/>
					@Copyright 2010-2014 <a href="http://baderlab.org/" target="_blank">UofT</a> &amp;
					<a href="https://www.cbio.mskcc.org" target="_blank">MSKCC</a>. Version ${project.version}.
					<a href="https://www.gnu.org/licenses/lgpl-3.0.txt">
						<img src="<spring:url value='/images/lgplv3-44x16.png'/>" width="44"  height="16"/>
					</a>
			  </p>	
		 </div>
	</div>	
</footer>
