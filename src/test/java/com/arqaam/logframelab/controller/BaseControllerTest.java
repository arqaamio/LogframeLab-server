package com.arqaam.logframelab.controller;

import com.arqaam.logframelab.model.Error;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "integration")
@EnableRuleMigrationSupport
public class BaseControllerTest {
    @Autowired
    protected TestRestTemplate testRestTemplate;

    Logger logger = Logger.getLogger(this.getClass().getName());

    void assertEqualsException(ResponseEntity<Error> response, HttpStatus httpStatus, Integer code, Class exception) {
        assertEquals(httpStatus, response.getStatusCode());
        assertEquals(code, response.getBody().getCode());
        assertEquals(exception.getSimpleName(), response.getBody().getException());
    }
}
