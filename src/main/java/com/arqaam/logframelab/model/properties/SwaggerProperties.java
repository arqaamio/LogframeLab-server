package com.arqaam.logframelab.model.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@AllArgsConstructor
@Data
@NoArgsConstructor
@ConfigurationProperties("prop.swagger")
public class SwaggerProperties {

    private String title;
    private String description;
    private String version;
    private String name;
    private String url;
    private String email;
    private String basePackage;
    private String license;
    private String licenseUrl;
}
