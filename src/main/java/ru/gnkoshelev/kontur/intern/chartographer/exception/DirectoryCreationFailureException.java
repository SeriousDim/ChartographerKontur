package ru.gnkoshelev.kontur.intern.chartographer.exception;

public class DirectoryCreationFailureException extends Exception {

    public DirectoryCreationFailureException(String invalidPath) {
        super("Неверное название для директории: " + invalidPath);
    }
}
