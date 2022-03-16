package ru.gnkoshelev.kontur.intern.chartographer.config;

import org.springframework.stereotype.Component;

@Component("mainConfig")
public class MainConfig {

    public static String bmpPath = "files";

    public static int UNIQUE_ID_SYMBOLS_LENGTH = 6;

    public static final int MAX_WIDTH = 20000;
    public static final int MAX_HEIGHT = 50000;

    public static final int MAX_FRAGMENT_WIDTH = 5000;
    public static final int MAX_FRAGMENT_HEIGHT = 5000;

    public static final String HINT = "\n\nДля справки обратитесь по адресу /chartas/ (GET-запрос)";
    public static final String INFO = "Добро пожаловать в сервис Chartographer - " +
            "сервис для восстановления изображений древних свитков и папирусов." +
            "" +
            "\n\n" + "Разработчик: Дмитрий Лыков (github.com/SeriousDim). 2022" +
            "\n" + "Специально для СКБ Контур";

}
