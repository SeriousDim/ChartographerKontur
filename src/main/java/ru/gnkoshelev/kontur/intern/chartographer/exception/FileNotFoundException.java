/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer.exception;

public class FileNotFoundException extends Exception{

    public FileNotFoundException(String fileName) {
        super("Файл с таким именем не найден: " + fileName);
    }
}
