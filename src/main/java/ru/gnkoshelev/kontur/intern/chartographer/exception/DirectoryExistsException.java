package ru.gnkoshelev.kontur.intern.chartographer.exception;

public class DirectoryExistsException extends Exception{

    public DirectoryExistsException(String path) {
        super("Directory with such path already exists: " + path);
    }

}
