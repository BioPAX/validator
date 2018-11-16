package org.biopax.validator.web.service;

import org.biopax.validator.ExceptionsAspect;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@Configuration
@ComponentScan
@ImportResource({"classpath*:META-INF/spring/appContext-validator.xml"})
//and instead appContext-loadTimeWeaving.xml, we use the following annotations: TODO seems cannot not catch unknown.property syntax.error
@EnableSpringConfigured //enables AOP
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
@EnableAspectJAutoProxy
public class ValidatorConfiguration {

  @Bean // to inject into InfoController
  public Properties errorTypes() throws IOException {
    return PropertiesLoaderUtils.loadAllProperties("codes.properties");
  }

  @Bean
  public ExceptionsAspect exceptionsAspect() {
    return new ExceptionsAspect();
  }
}
