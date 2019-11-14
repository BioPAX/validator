package org.biopax.validator.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;


@SpringBootApplication
@EnableWebMvc
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

  @Bean(name = "multipartResolver")
  public CommonsMultipartResolver multipartResolver() {
    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    multipartResolver.setMaxUploadSize(-1);
    return multipartResolver;
  }

}
