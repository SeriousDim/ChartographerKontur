package ru.gnkoshelev.kontur.intern.chartographer.universal;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Класс с собственными математическими функциями
 */
public class MathManager {

    public static boolean isBetween(int value, int start, int end) {
        return value >= start && value <= end;
    }

    /**
     * @param input     - точка для преобразования, заданная
     *                  в старой системе координат
     * @param oldOrigin - точка отсчета старой системы координат,
     *                  *           заданная в старой системе координат
     * @param newOrigin - точка отсчета новой системы координат,
     *                  заданная в старой системе координат
     * @return точка input. заданная в новой системе координат
     */
    public static Point transformPoint(Point input, Point oldOrigin, Point newOrigin) {
        var nx = oldOrigin.x - newOrigin.x + input.x;
        var ny = oldOrigin.y - newOrigin.y + input.y;
        return new Point(nx, ny);
    }

    public static Rectangle transformRect(Rectangle input, Point oldOrigin, Point newOrigin) {
        var result = new Rectangle();
        var newLeftTop = transformPoint(new Point(input.x, input.y), oldOrigin, newOrigin);

        result.x = newLeftTop.x;
        result.y = newLeftTop.y;
        result.width = input.width;
        result.height = input.height;

        return result;
    }

    public static Rectangle getFragmentIntersection(int x, int y, int width, int height,
                                             int imgWidth, int imgHeight) {
        var rFrag = new Rectangle(x, y, width, height);
        var rImg = new Rectangle(0, 0, imgWidth, imgHeight);
        return rImg.intersection(rFrag);
    }

}
