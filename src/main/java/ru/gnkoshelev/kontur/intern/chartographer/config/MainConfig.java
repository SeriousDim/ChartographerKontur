/*
 * Дмитрий Лыков, 2022
 */
package ru.gnkoshelev.kontur.intern.chartographer.config;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component("mainConfig")
public class MainConfig {

    public static String bmpPath = "files";

    public static int UNIQUE_ID_SYMBOLS_LENGTH = 6;

    public static final int MAX_WIDTH = 20000;
    public static final int MAX_HEIGHT = 50000;
    public static final int MAX_FRAGMENT_WIDTH = 5000;
    public static final int MAX_FRAGMENT_HEIGHT = 5000;

    public static final String HEAD_ROUTE = "/chartas";

    public static final String TEXTS_RU = "texts/ru/";
    public static final String HINT = "\n\nДля справки обратитесь по адресу " +
            "/chartas/ (GET-запрос)";

    public static final String CANNOT_CROP_IMAGE_MESSAGE =
            "Не удалось обрезать изображение. Размеры " +
            "изображения: %dx%d. Попытка вырезать " +
            "следующий прямоугольник: x = %d, y = %d, ширина = %d, " +
            "высота = %d.\n" +
            "Проверьте, что все значения больше нуля";

}
