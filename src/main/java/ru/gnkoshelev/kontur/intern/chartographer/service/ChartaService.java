package ru.gnkoshelev.kontur.intern.chartographer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gnkoshelev.kontur.intern.chartographer.exception.FileNotFoundException;
import ru.gnkoshelev.kontur.intern.chartographer.component.BmpManager;
import ru.gnkoshelev.kontur.intern.chartographer.exception.ParamOutOfBounds;

import java.io.IOException;

/**
 * Spring-сервис, обрабатывающий запросы. Использует {@link BmpManager BmpManager}
 * Содержит логику обработки запросов: проверяет корректность присланных данных
 * и параметров.
 */
@Service
public class ChartaService {

    @Autowired
    private BmpManager manager;

    public String createCanvas(int width, int height) {
        var id = manager.generateId();

        manager.createNewFile(id, width, height);

        return id;
    }

    public byte[] getChartaFragment(String fileId,
                                  int x, int y,
                                  int width, int height)
            throws FileNotFoundException, ParamOutOfBounds, IOException {
        var bytes = manager.getFragement(fileId, x, y, width, height);

        return bytes;
    }

    public byte[] getWholeCharta(String fileId)
            throws FileNotFoundException {
        var bytes = manager.readFileAsBytes(fileId);

        return bytes;
    }

}
