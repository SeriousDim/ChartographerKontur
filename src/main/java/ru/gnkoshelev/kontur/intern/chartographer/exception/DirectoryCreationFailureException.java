/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer.exception;

/**
 * Исключение, связанное с ошибками при работе с директориями
 * (кроме существования директории, см. {@link DirectoryExistsException})
 */
public class DirectoryCreationFailureException extends Exception {

    public DirectoryCreationFailureException(String invalidPath) {
        super("Неверное название для директории: " + invalidPath);
    }
}
