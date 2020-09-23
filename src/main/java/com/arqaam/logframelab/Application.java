package com.arqaam.logframelab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.arqaam.logframelab.model.properties")
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
