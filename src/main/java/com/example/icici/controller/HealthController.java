package com.example.icici.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthController {

    @PostMapping("/health")
    public String health() throws Exception{

        System.out.println("ICICI Bank Service Is Now Active!!!");
        return "ICICI Bank Service Is Now Active!!!";
    }
}
