package ru.gnkoshelev.kontur.intern.chartographer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
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

    public String createCanvas(int width, int height) throws ParamOutOfBounds {
        if (!manager.isBetween(width, 0, MainConfig.MAX_WIDTH)){
            throw new ParamOutOfBounds("width",
                    "[0, " + MainConfig.MAX_WIDTH + "]");
        }
        if (!manager.isBetween(height, 0, MainConfig.MAX_HEIGHT)){
            throw new ParamOutOfBounds("height",
                    "[0, " + MainConfig.MAX_HEIGHT + "]");
        }

        var id = manager.generateId();

        manager.createNewFile(id, width, height);

        return id;
    }

    public void saveFragment(String fileName,
                             byte[] fragmentBytes,
                             int x, int y,
                             int width, int height)
            throws FileNotFoundException, ParamOutOfBounds, IOException {
        manager.saveFragment(fileName, fragmentBytes, x, y, width, height);
    }

    public byte[] getChartaFragment(String fileId,
                                  int x, int y,
                                  int width, int height)
            throws FileNotFoundException, ParamOutOfBounds, IOException {
        if (!manager.isFragmentWidthCorrect(width)) {
            throw new ParamOutOfBounds("width",
                    "[0, " + MainConfig.MAX_FRAGMENT_WIDTH + "]");
        }
        if (!manager.isFragmentHeightCorrect(height)) {
            throw new ParamOutOfBounds("height",
                    "[0, " + MainConfig.MAX_FRAGMENT_HEIGHT + "]");
        }

        var bytes = manager.getFragement(fileId, x, y, width, height);

        return bytes;
    }

    public boolean deleteCanvas(String fileId) throws FileNotFoundException {
        return manager.deleteFile(fileId);
    }

    public byte[] getWholeCharta(String fileId)
            throws FileNotFoundException {
        var bytes = manager.readFileAsBytes(fileId);

        return bytes;
    }

}
