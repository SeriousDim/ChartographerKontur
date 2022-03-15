package ru.gnkoshelev.kontur.intern.chartographer.helpers;

import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;

public class Responder extends HttpHeaders {

    public static final MediaType IMAGE_BMP = MediaType.parseMediaType("image/bmp");
    public static final MediaType APP_JSON = MediaType.APPLICATION_JSON;
    public static final MediaType TEXT_PLAIN = MediaType.TEXT_PLAIN;

    public static HttpHeaders generateHeaders(MediaType type) {
        var result = new HttpHeaders();
        result.setContentType(type);
        return result;
    }

    public static void showException(Exception e, Logger logger) {
        var message = e.getClass().getSimpleName() + " : " + e.getMessage();
        logger.error(message);
        e.printStackTrace();
    }

    public static <T> ResponseEntity<T> processException(Exception e, Logger logger) {
        var message = e.getClass().getSimpleName() + " : " + e.getMessage();
        showException(e, logger);
        return (ResponseEntity<T>) Responder.respondText(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static <T> ResponseEntity<T> respond(T body, MediaType type, HttpStatus status) {
        var headers = generateHeaders(type);

        return new ResponseEntity<>(body, headers, status);
    }

    public static <T> ResponseEntity<T> respondBmp(T body, HttpStatus status) {
        return respond(body, IMAGE_BMP, status);
    }

    public static <T> ResponseEntity<T> respondText(T body, HttpStatus status) {
        if (body instanceof String && status != HttpStatus.OK) {
            var newBody = (String)body + MainConfig.HINT;
            return (ResponseEntity<T>) respond(newBody, TEXT_PLAIN, status);
        }

        return respond(body, TEXT_PLAIN, status);
    }

    public static <T> ResponseEntity<T> respondJson(T body, HttpStatus status) {
        return respond(body, APP_JSON, status);
    }

}
