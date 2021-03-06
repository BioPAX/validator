<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<header class="header">
	<nav id="header_navbar" class="navbar navbar-default navbar-fixed-top" role="navigation">
        <div class="container">
              <div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#top-navbar-collapse">
				  <span class="sr-only">Toggle navigation</span>
				  <span class="icon-bar"></span>
				  <span class="icon-bar"></span>
				  <span class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="https://www.biopax.org" target="_blank">
				 BioPAX.org
				</a>
			  </div>              
              <div class="collapse navbar-collapse pull-right" id="top-navbar-collapse">
                  <ul class="nav navbar-nav">
					<li><a href="home">Home</a></li>
					<li><a href="check">Validate</a></li>
					<li class="dropdown">
                  		<a href="#" class="dropdown-toggle" data-toggle="dropdown">Info<b class="caret"></b></a>
                  		<span class="dropdown-arrow"></span>
                  		<ul class="dropdown-menu">
							<li><a href="ws">Web Service</a></li>
							<li><a href="rules">BioPAX Rules</a></li>
							<li><a href="errorTypes">Errors</a></li>
							<li><a href="extraCfg">Other</a></li>
                    	</ul>
                	</li>
					<li class="dropdown">
                  		<a href="#" class="dropdown-toggle" data-toggle="dropdown">Links<b class="caret"></b></a>
                  		<span class="dropdown-arrow"></span>
                  		<ul class="dropdown-menu">
							<li><a href="https://github.com/BioPAX/validator/wiki" target="_blank">Wiki (Validator)</a></li>
							<li><a href="https://github.com/BioPAX/validator/wiki/BioPAXRules" target="_blank">Wiki (Rules)</a></li>
							<li><a href="https://biopax.github.io/validator/" target="_blank">Project, Docs</a></li>
							<li><a href="https://www.pathwaycommons.org" target="_blank">Pathway Commons</a></li>
							<li><a href="http://www.ebi.ac.uk/miriam/" target="_blank">MIRIAM</a></li>
							<li><a href="http://www.obofoundry.org/" target="_blank">OBO</a></li>
                    	</ul>
                	</li>
					<li><a href="https://github.com/BioPAX/validator/issues" target="_blank">Report</a></li>
					<li><a href="#" class="top-scroll">Top</a></li>
                  </ul>
          	  </div> <%-- collapse --%>
      </div> <%-- container --%>
 	</nav>
</header>

<div class="container">
<%-- to be closed in the footer.jsp --%>