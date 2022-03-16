package ru.gnkoshelev.kontur.intern.chartographer.universal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public class IdGenerator {

    /**
     * Сгенерировать уникальный человекочитабельный
     * идентификатор
     *
     * @param symbols - кол-во случайных уникальных символов в
     * идентификаторе
     * @return уникальный id в формате: "dd-MM-yy-xxxxxx", где
     * dd-MM-yy - сегодняшняя дата, хххххх - кол-во случайных
     * уникальных символов, задаваемых числом symbols
     */
    public static String generateIdForHuman(int symbols) {
        var id = UUID.randomUUID().toString();
        id = id.replace("-", "");
        var idLength = id.length();

        if (symbols < 0 || symbols > idLength - 1) {
            throw new IllegalArgumentException("symbols должен быть от 0 до " + (idLength - 1) + "вкл.");
        }

        var formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
        var dateStr = formatter.format(LocalDate.now());

        var beginInd = new Random().nextInt(idLength - symbols - 1);

        return dateStr + "-" + id.substring(beginInd, beginInd + symbols);
    }

}
