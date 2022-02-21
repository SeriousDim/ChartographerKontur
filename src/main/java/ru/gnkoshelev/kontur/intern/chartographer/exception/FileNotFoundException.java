package ru.gnkoshelev.kontur.intern.chartographer.exception;

public class FileNotFoundException extends Exception{

    public FileNotFoundException(String fileName) {
        super("File not found: " + fileName);
    }
}
