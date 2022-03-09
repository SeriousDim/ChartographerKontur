package ru.gnkoshelev.kontur.intern.chartographer.controller;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
import ru.gnkoshelev.kontur.intern.chartographer.exception.ParamOutOfBounds;
import ru.gnkoshelev.kontur.intern.chartographer.helpers.Responder;
import ru.gnkoshelev.kontur.intern.chartographer.exception.FileNotFoundException;
import ru.gnkoshelev.kontur.intern.chartographer.service.ChartaService;
import ru.gnkoshelev.kontur.intern.chartographer.helpers.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Spring-контроллер, в котором реализован API приложения
 */
@RestController
@RequestMapping("/chartas")
public class ApiController {

    @Autowired
    private ChartaService service;

    private Logger logger;


    @GetMapping("/")
    public ResponseEntity<String> info() {
        return Responder.respondText(MainConfig.INFO, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<String> createCanvas(@RequestParam int width,
                                       @RequestParam int height) {
        var result = service.createCanvas(width, height);
        return Responder.respondText(result, HttpStatus.OK);
    }

    @PostMapping(value = "/{id}/")
    public ResponseEntity saveFragment(@PathVariable String id,
                                       @RequestParam int x,
                                       @RequestParam int y,
                                       @RequestParam int width,
                                       @RequestParam int height,
                                       InputStream fileStream) {
        return ResponseEntity.ok("Ok");
    }

    @GetMapping(value = "/{id}/")
    public ResponseEntity<?> getFragment(@PathVariable String id,
                                         @RequestParam int x,
                                         @RequestParam int y,
                                         @RequestParam int width,
                                         @RequestParam int height) {
        logger = Log.get("ApiController");

        try {
            var result = service.getChartaFragment(id, x, y, width, height);
            return Responder.respondBmp(result, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            return Responder.respondText(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (ParamOutOfBounds e) {
            return Responder.respondText(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            var message = e.getClass().getSimpleName() + " : " + e.getMessage();
            logger.error(message);
            e.printStackTrace();
            return Responder.respondText(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteCanvas() {
        return ResponseEntity.ok("TestMapping");
    }

}
