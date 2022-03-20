/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer.config;

import org.springframework.stereotype.Component;

/**
 * Конфигурация приложения
 */
@Component("mainConfig")
public class MainConfig {

    // директория с изображениями
    public static String bmpPath = "files";

    // кол-во уникальных символов для id изображений
    public static int UNIQUE_ID_SYMBOLS_LENGTH = 6;

    // макс. возможные размеры изображений
    public static final int MAX_WIDTH = 20000;
    public static final int MAX_HEIGHT = 50000;

    // макс. возможные размеры фрагментов
    public static final int MAX_FRAGMENT_WIDTH = 5000;
    public static final int MAX_FRAGMENT_HEIGHT = 5000;

    public static final String HEAD_ROUTE = "/chartas";

    public static final String TEXTS_RU = "texts/ru/";

    // подсказка при ошибках
    public static final String HINT = "\n\nДля справки обратитесь по адресу " +
            "/chartas/ (GET-запрос)";

    public static final String CANNOT_CROP_IMAGE_MESSAGE =
            "Не удалось обрезать изображение. Размеры " +
            "изображения: %dx%d. Попытка вырезать " +
            "следующий прямоугольник: x = %d, y = %d, ширина = %d, " +
            "высота = %d.\n" +
            "Проверьте, что все значения больше нуля";

}
