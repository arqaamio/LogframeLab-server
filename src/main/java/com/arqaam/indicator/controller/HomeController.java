package com.arqaam.indicator.controller;

import com.arqaam.indicator.service.IndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class HomeController {

    @GetMapping()
    public String home (){
        return "Indicator web app";
    }

}
