package org.biopax.validator.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@SpringBootApplication
public class Application implements WebMvcConfigurer {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(Application.class);
    application.run(args);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
  }

  @Override
  public void configureViewResolvers(ViewResolverRegistry registry) {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setPrefix("/jsp/");
    resolver.setSuffix(".jsp");
    resolver.setViewClass(JstlView.class);
    registry.viewResolver(resolver);
  }

}
