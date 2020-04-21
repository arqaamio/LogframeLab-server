package com.arqaam.indicator.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "Home")
public class HomeController {

    @GetMapping()
    public String home (){
        return "Indicator web app";
    }

}
