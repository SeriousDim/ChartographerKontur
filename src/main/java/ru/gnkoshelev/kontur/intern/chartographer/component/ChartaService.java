package ru.gnkoshelev.kontur.intern.chartographer.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gnkoshelev.kontur.intern.chartographer.exception.FileNotFoundException;
import ru.gnkoshelev.kontur.intern.chartographer.universal.BmpManager;

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

    public byte[] getWholeFile(String imgName)
            throws FileNotFoundException, IOException {
        var bytes = manager.getFile(imgName);

        if (bytes == null) {
            throw new FileNotFoundException(manager.getFilePath(imgName));
        }

        return bytes;
    }

    public byte[] getFragment() {
        return null;
    }

}
