package ru.gnkoshelev.kontur.intern.chartographer.exception;

public class DirectoryCreationFailureException extends Exception {

    public DirectoryCreationFailureException(String invalidPath) {
        super("Cannot create the directory with such path: " + invalidPath);
    }
}
