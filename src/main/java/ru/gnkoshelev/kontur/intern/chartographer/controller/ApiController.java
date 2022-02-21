package ru.gnkoshelev.kontur.intern.chartographer.controller;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.gnkoshelev.kontur.intern.chartographer.component.ChartaService;
import ru.gnkoshelev.kontur.intern.chartographer.config.Log;
import ru.gnkoshelev.kontur.intern.chartographer.exception.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/chartas")
public class ApiController {

    @Autowired
    private ChartaService service;

    private Logger logger;

    @PostMapping("/")
    public ResponseEntity createCanvas(@RequestParam int width,
                                       @RequestParam int height) {
        var result = service.createCanvas(width, height);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    // MULTIPART_FORM_DATA_VALUE
    //@PostMapping(value = "/{id}/", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @RequestMapping(value = "/{id}/", method = RequestMethod.POST)
    public ResponseEntity saveFragment(@PathVariable String id,
                                       @RequestParam int x,
                                       @RequestParam int y,
                                       @RequestParam int width,
                                       @RequestParam int height,
                                       InputStream fileStream) {
        return ResponseEntity.ok("Ok");
    }

    @GetMapping(value = "/", produces = "image/bmp")
    public ResponseEntity getFragment() throws FileNotFoundException, IOException {
        logger = Log.get("ApiController");

        var result = service.getWholeFile("1");
        //HttpHeaders responseHeaders = new HttpHeaders();
        //responseHeaders.setContentType(MediaType.parseMediaType("image/bmp"));

        return new ResponseEntity(result, HttpStatus.OK);
    }

    @DeleteMapping("")
    public ResponseEntity deleteCanvas() {
        return ResponseEntity.ok("TestMapping");
    }

}
