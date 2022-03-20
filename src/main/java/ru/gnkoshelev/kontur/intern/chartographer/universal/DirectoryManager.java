/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer.universal;

import ru.gnkoshelev.kontur.intern.chartographer.exception.*;

import java.io.File;

/**
 * Класс для работы с директориями в файловой системе
 */
public class DirectoryManager {

    /**
     * Удаляет символы слешей в начале строки, чтобы потом ее
     * можно было передать в конструктор {@link File}
     * @param path
     * @return строка path без ведущих слешей
     */
    public static String removeLeadSlash(String path) {
        if (path.startsWith("\\\\")) {
            return path.substring(2);
        } else if (path.startsWith("/") ||
                path.startsWith("\\")) {
            return path.substring(1);
        }

        return path;
    }

    /**
     * Создает директорию с данным названием, если ее не сущетвеут
     * @param path название директории
     * @throws DirectoryCreationFailureException при любой иной ошибке при
     * создании директории
     * @throws DirectoryExistsException если директория уже существует
     */
    public static void tryCreateDirectory(String path)
            throws DirectoryCreationFailureException, DirectoryExistsException {
        var dir = new File(path);

        if (dir.exists()) {
            throw new DirectoryExistsException(path);
        }

        var result = dir.mkdirs();
        if (!result) {
            throw new DirectoryCreationFailureException(path);
        }
    }

    /**
     * Используйте {@link DirectoryManager#tryCreateDirectory(String)}, в него
     * уже встроена проверка правильности пути
     */
    @Deprecated
    public static boolean isValidPath(String path) {
        return !path.isBlank() &&
                (path.contains("/") ||
                        path.contains("\\"));
    }

}
