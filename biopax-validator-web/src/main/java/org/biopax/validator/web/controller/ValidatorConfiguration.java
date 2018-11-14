package org.biopax.validator.web.controller;

import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@Configuration
@ImportResource({"classpath*:META-INF/spring/appContext-validator.xml"})
//and instead appContext-loadTimeWeaving.xml, we use AOP+LTW the following annotations:
@EnableSpringConfigured //enables AOP
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
@EnableAspectJAutoProxy
public class ValidatorConfiguration {

  @Bean // to inject into InfoController
  public Properties errorTypes() throws IOException {
    return PropertiesLoaderUtils.loadAllProperties("codes.properties");
  }

}
