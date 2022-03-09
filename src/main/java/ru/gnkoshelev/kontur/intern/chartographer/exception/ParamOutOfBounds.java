package ru.gnkoshelev.kontur.intern.chartographer.exception;

public class ParamOutOfBounds extends Exception {

    public ParamOutOfBounds(String paramName, String restrictions) {
        super("Один или несколько параметров заданы вне допустимых значений: " + paramName + ". " +
                "Допустимые значения: " + restrictions);
    }

}
