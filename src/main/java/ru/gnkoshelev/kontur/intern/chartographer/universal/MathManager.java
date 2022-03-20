/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer.universal;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Класс с собственными математическими функциями
 */
public class MathManager {

    /**
     * Проверяет принадлежность значения value интервалу, включая границы
     * @param value значение для проверки
     * @param start начало интервала
     * @param end конец интервала
     * @return
     */
    public static boolean isBetween(int value, int start, int end) {
        return value >= start && value <= end;
    }

    /**
     * Преобразует точку из одной системы координат в другую
     * @param input     точка для преобразования, заданная
     *                  в старой системе координат
     * @param oldOrigin точка отсчета старой системы координат,
     *                  заданная в старой системе координат
     * @param newOrigin точка отсчета новой системы координат,
     *                  заданная в старой системе координат
     * @return точка input. заданная в новой системе координат
     */
    public static Point transformPoint(Point input, Point oldOrigin, Point newOrigin) {
        var nx = oldOrigin.x - newOrigin.x + input.x;
        var ny = oldOrigin.y - newOrigin.y + input.y;
        return new Point(nx, ny);
    }

    /**
     * Аналогично {@link MathManager#transformPoint(Point, Point, Point)},
     * преобразует прямоугольник {@link Rectangle} из одной системы координат в
     * другую
     * @param input прямоугольник, точки которого заданы в старой системе координат
     * @param oldOrigin точка отсчета старой системы координат,
     *                  заданная в старой системе координат
     * @param newOrigin точка отсчета новой системы координат,
     *                  заданная в старой системе координат
     * @return
     */
    public static Rectangle transformRect(Rectangle input, Point oldOrigin, Point newOrigin) {
        var result = new Rectangle();
        var newLeftTop = transformPoint(new Point(input.x, input.y), oldOrigin, newOrigin);

        result.x = newLeftTop.x;
        result.y = newLeftTop.y;
        result.width = input.width;
        result.height = input.height;

        return result;
    }

    /**
     * Находит пересечение фрагмента изображения с самим изображением.
     * Предполагается, что левая верхняя точка изображения находится в точке
     * (0, 0)
     * @param x координата x левой верхней точки фрагмента
     * @param y координата y левой верхней точки фрагмента
     * @param width ширина фрагмента
     * @param height высота фрагмента
     * @param imgWidth ширина изображения
     * @param imgHeight ширина изображения
     * @return
     */
    public static Rectangle getFragmentIntersection(int x, int y, int width, int height,
                                             int imgWidth, int imgHeight) {
        var rFrag = new Rectangle(x, y, width, height);
        var rImg = new Rectangle(0, 0, imgWidth, imgHeight);
        return rImg.intersection(rFrag);
    }

}
