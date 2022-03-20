/*
 * Дмитрий Лыков, 2022
 */
package ru.gnkoshelev.kontur.intern.chartographer.controller;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Text;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
import ru.gnkoshelev.kontur.intern.chartographer.exception.FileNotFoundException;
import ru.gnkoshelev.kontur.intern.chartographer.exception.ParamOutOfBounds;
import ru.gnkoshelev.kontur.intern.chartographer.helpers.Log;
import ru.gnkoshelev.kontur.intern.chartographer.helpers.Responder;
import ru.gnkoshelev.kontur.intern.chartographer.service.ChartaService;
import ru.gnkoshelev.kontur.intern.chartographer.universal.TextResourceManager;

import java.io.IOException;

/**
 * Spring-контроллер, в котором реализован API приложения
 */
@RestController
@RequestMapping(MainConfig.HEAD_ROUTE)
public class ApiController {

    @Autowired
    private ChartaService service;

    private Logger logger;


    @GetMapping("/")
    public ResponseEntity<String> info() throws IOException {
        var info = TextResourceManager.getText("info.txt");
        return Responder.respondText(info, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<String> createCanvas(@RequestParam int width,
                                               @RequestParam int height) {
        logger = Log.get("ApiController");

        try {
            var result = service.createCharta(width, height);
            return Responder.respondText(result, HttpStatus.CREATED);
        } catch (ParamOutOfBounds e) {
            return Responder.respondText(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return Responder.processException(e, logger);
        }
    }

    @PostMapping(value = "/{id}/")
    public ResponseEntity saveFragment(@PathVariable String id,
                                       @RequestParam int x,
                                       @RequestParam int y,
                                       @RequestParam int width,
                                       @RequestParam int height,
                                       @RequestBody byte[] fileStream) {
        logger = Log.get("ApiController");

        try {
            service.saveChartaFragment(id, fileStream, x, y, width, height);
            return Responder.respondBmp(null, HttpStatus.OK);
        } catch (ParamOutOfBounds | IOException e) {
            e.printStackTrace();
            return Responder.respondText(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Responder.respondText(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return Responder.processException(e, logger);
        }
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
            return Responder.processException(e, logger);
        }
    }

    @DeleteMapping(value = "/{id}/")
    public ResponseEntity<?> deleteCanvas(@PathVariable String id) {
        try {
            var result = service.deleteCharta(id);
            if (result) {
                return Responder.respondText(null, HttpStatus.OK);
            } else {
                return Responder.respondText(null, HttpStatus.BAD_REQUEST);
            }
        } catch (FileNotFoundException e) {
            return Responder.respondText(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return Responder.processException(e, logger);
        }
    }

}
