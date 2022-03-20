/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer.exception;

public class ParamOutOfBounds extends Exception {

    public ParamOutOfBounds(String paramNames, String restrictions) {
        super("Один или несколько параметров заданы вне допустимых значений: " + paramNames + ". " +
                "Допустимые значения: " + restrictions);
    }

}
