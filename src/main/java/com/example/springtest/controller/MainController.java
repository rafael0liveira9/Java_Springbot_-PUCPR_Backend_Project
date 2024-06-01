package com.example.springtest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/")
    public ResponseEntity<String> greet() {
        return ResponseEntity.ok("*** STATUS: OK, PROJETO RODANDO ***");
    }
}