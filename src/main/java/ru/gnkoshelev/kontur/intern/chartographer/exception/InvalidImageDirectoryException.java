package ru.gnkoshelev.kontur.intern.chartographer.exception;

public class InvalidImageDirectoryException extends Exception {

    public InvalidImageDirectoryException(String invalidPath) {
        super("Invalid path: " + invalidPath);
    }
}
