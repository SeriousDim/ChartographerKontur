package ru.gnkoshelev.kontur.intern.chartographer.config;

import org.springframework.stereotype.Component;

@Component("mainConfig")
public class MainConfig {

    public static String bmpPath = "files";

    public static final int MAX_WIDTH = 20000;
    public static final int MAX_HEIGHT = 50000;

}
