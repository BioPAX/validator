package org.biopax.validator.ws;

//import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@Configuration
//@SpringBootConfiguration
@ImportResource({"classpath:META-INF/spring/appContext-validator.xml"})
//  , "classpath:META-INF/spring/appContext-loadTimeWeaving.xml"})
@EnableSpringConfigured //enables AOP
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
@EnableAspectJAutoProxy
public class ValidatorConfiguration {

  @Bean // to inject into InfoController
  public Properties errorTypes() throws IOException {
    return PropertiesLoaderUtils.loadAllProperties("codes.properties");
  }

}
