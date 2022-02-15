package ru.gnkoshelev.kontur.intern.chartographer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gnkoshelev.kontur.intern.chartographer.universal.FileManager;

import java.io.InputStream;

@RestController
@RequestMapping("/chartas")
public class ApiController {

    @Autowired
    FileManager fileManager;

    @PostMapping("/")
    public ResponseEntity createCanvas(@RequestParam int width,
                                       @RequestParam int height) {
        System.out.println(fileManager.getImageDirectory());
        return new ResponseEntity<>(String.format("width: %d, height: %d", width, height), HttpStatus.CREATED);
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
        //try {


            return ResponseEntity.ok("Ok");
        /*} catch (IOException e) {
            return ResponseEntity.badRequest().body("IOException");
        }*/
        /*String result = String.format("Name: %s, size: %d\nx = %d, y = %d, w = %d, h = %d",
                file.getOriginalFilename(),
                file.getSize(),
                x,
                y,
                width,
                height);
        return ResponseEntity.ok(result);*/
    }

    @GetMapping("")
    public ResponseEntity getFragment() {
        return ResponseEntity.ok("TestMapping");
    }

    @DeleteMapping("")
    public ResponseEntity deleteCanvas() {
        return ResponseEntity.ok("TestMapping");
    }

}
