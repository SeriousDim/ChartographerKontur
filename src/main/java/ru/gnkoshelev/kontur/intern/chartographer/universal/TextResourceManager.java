/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer.universal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Класс для работы с ресурсами приложения.
 * Ресурсы хранятся в папке src/main/resources/
 */
public class TextResourceManager {

    /**
     * Прочиать и вернуть текст в файле в папке с ресурсами
     * @param fileName имя файла в папке с ресурсами
     * @return текст из файла
     * @throws IOException
     */
    public static String getText(String fileName) throws IOException {
        var path = "classpath:" + fileName;
        var res = TextResourceManager.class.getClassLoader().getResourceAsStream(path);
        var value = new String(res.readAllBytes(), StandardCharsets.UTF_8);
        return value;
    }

}
