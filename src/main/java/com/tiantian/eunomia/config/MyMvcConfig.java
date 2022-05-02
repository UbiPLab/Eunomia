package com.tiantian.eunomia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author tiantian152
 */
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/index.html").setViewName("index");

        registry.addViewController("/page-login.html").setViewName("page-login");
        registry.addViewController("/page-register.html").setViewName("page-register");
        registry.addViewController("/page-invoices.html").setViewName("page-invoices");
        registry.addViewController("/page-profile.html").setViewName("page-profile");
        registry.addViewController("/forms-validation.html").setViewName("forms-validation");
        registry.addViewController("/page-createCase.html").setViewName("page-createCase");
        registry.addViewController("/page-caseList.html").setViewName("page-caseList");
        registry.addViewController("/Monitor.html").setViewName("Monitor");
        registry.addViewController("/page-success.html").setViewName("page-success");
        registry.addViewController("/welcome.html").setViewName("welcome");
        registry.addViewController("/steps.html").setViewName("steps");
        registry.addViewController("/close.html").setViewName("page-close");
        registry.addViewController("/DefectorTracking.html").setViewName("DefectorTracking");

        registry.addViewController("/provider-login").setViewName("Provider/provider-login");
        registry.addViewController("/provider-register").setViewName("Provider/provider-register");


    }

    @Bean
    public LocaleResolver localeResolver() {
        return new MyLocaleResolver();
    }
}
