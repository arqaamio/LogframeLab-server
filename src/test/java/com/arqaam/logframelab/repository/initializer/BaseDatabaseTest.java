package com.arqaam.logframelab.repository.initializer;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = BaseDatabaseTest.Initializer.class)
@Testcontainers
public interface BaseDatabaseTest {

  @Rule ErrorCollector collector = new ErrorCollector();

  @Container
  MySQLContainer<?> mySQLContainer =
      new MySQLContainer<>("mysql:5.7")
          .withDatabaseName("integration_tests")
          .withUsername("root")
          .withPassword("")
          .withPrivilegedMode(true);

  @TestPropertySource(locations = "classpath:application-integration.properties")
  class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
      TestPropertyValues.of(
              "spring.datasource.url=" + mySQLContainer.getJdbcUrl(),
              "spring.flyway.url=" + mySQLContainer.getJdbcUrl())
          .applyTo(applicationContext.getEnvironment());
    }
  }
}
