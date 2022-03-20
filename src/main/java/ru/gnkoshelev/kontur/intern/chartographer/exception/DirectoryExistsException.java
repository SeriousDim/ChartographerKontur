/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer.exception;

public class DirectoryExistsException extends Exception{

    public DirectoryExistsException(String path) {
        super("Директория с таким именем уже существует: " + path);
    }

}
