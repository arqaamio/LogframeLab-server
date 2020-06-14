package com.arqaam.logframelab.configuration;

import com.arqaam.logframelab.model.properties.SwaggerProperties;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final SwaggerProperties swaggerProperties;

    public GlobalConfiguration(SwaggerProperties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    @Bean
    public Docket api() {
        Contact contact = new Contact(swaggerProperties.getName(), swaggerProperties.getUrl(), swaggerProperties.getEmail());
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfo(swaggerProperties.getTitle(), swaggerProperties.getDescription(), swaggerProperties.getVersion(),
                        "", contact, swaggerProperties.getLicense(), swaggerProperties.getLicenseUrl(), new ArrayList<>()))
                .select()
                .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getBasePackage()))
                .paths(PathSelectors.any())
                .build();
    }
}
