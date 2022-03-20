/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

    public static <T> Logger get(Class<T> c) {
        return LoggerFactory.getLogger(String.format("app (%s)", c.getSimpleName()));
    }

    public static <T> Logger get(String className) {
        return LoggerFactory.getLogger(String.format("app (%s)", className));
    }

}
