package org.biopax.validator.web.service;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath:META-INF/spring/appContext-validator.xml",
"classpath:META-INF/spring/appContext-loadTimeWeaving.xml"})
public class ValidatorConfig {
}
