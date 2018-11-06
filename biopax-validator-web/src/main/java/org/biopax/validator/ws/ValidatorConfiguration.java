package org.biopax.validator.ws;

import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@Configuration
@EnableSpringConfigured //enables AOP
@ImportResource("classpath:META-INF/spring/appContext-validator.xml")
@ComponentScan(basePackages = {"org.biopax.validator"})
public class ValidatorConfiguration {

}
