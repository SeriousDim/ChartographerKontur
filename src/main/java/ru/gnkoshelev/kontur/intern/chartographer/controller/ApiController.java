package ru.gnkoshelev.kontur.intern.chartographer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chartas")
public class ApiController {

    @GetMapping
    public ResponseEntity test() {
        return ResponseEntity.ok("Works");
    }

}
