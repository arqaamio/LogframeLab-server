package com.arqaam.indicator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;

@Configuration
public class GlobalConfiguration {

    @Value("${prop.swagger.title}")
    private String title;

    @Value("${prop.swagger.description}")
    private String description;

    @Value("${prop.swagger.version}")
    private String version;

    @Value("${prop.swagger.name}")
    private String name;

    @Value("${prop.swagger.url}")
    private String url;

    @Value("${prop.swagger.email}")
    private String email;

    @Value("${prop.swagger.base-package}")
    private String basePackage;

    @Value("${prop.swagger.license}")
    private String license;

    @Value("${prop.swagger.license-url}")
    private String licenseUrl;

    @Bean
    public Docket api() {
        Contact contact = new Contact(name, url, email);
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfo(title, description, version,
                        "", contact, license, licenseUrl, new ArrayList<>()))
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build();
    }
}
