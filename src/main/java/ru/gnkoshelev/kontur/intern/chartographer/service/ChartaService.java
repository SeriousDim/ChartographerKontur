/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gnkoshelev.kontur.intern.chartographer.component.BmpManager;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
import ru.gnkoshelev.kontur.intern.chartographer.exception.FileNotFoundException;
import ru.gnkoshelev.kontur.intern.chartographer.exception.ParamOutOfBounds;
import ru.gnkoshelev.kontur.intern.chartographer.universal.MathManager;

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

    public String createCharta(int width, int height) throws ParamOutOfBounds {
        if (!MathManager.isBetween(width, 1, MainConfig.MAX_WIDTH)) {
            throw new ParamOutOfBounds("width",
                    "[1, " + MainConfig.MAX_WIDTH + "]");
        }
        if (!MathManager.isBetween(height, 1, MainConfig.MAX_HEIGHT)) {
            throw new ParamOutOfBounds("height",
                    "[1, " + MainConfig.MAX_HEIGHT + "]");
        }

        var id = manager.generateId();

        manager.createNewFile(id, width, height);

        return id;
    }

    public void saveChartaFragment(String fileName,
                                   byte[] fragmentBytes,
                                   int x, int y,
                                   int width, int height)
            throws FileNotFoundException, ParamOutOfBounds, IOException {
        manager.saveFragment(fileName, fragmentBytes, x, y, width, height);
    }

    public byte[] getChartaFragment(String fileName,
                                    int x, int y,
                                    int width, int height)
            throws FileNotFoundException, ParamOutOfBounds, IOException {
        if (!manager.isFragmentWidthCorrect(width)) {
            throw new ParamOutOfBounds("width",
                    "[1, " + MainConfig.MAX_FRAGMENT_WIDTH + "]");
        }
        if (!manager.isFragmentHeightCorrect(height)) {
            throw new ParamOutOfBounds("height",
                    "[1, " + MainConfig.MAX_FRAGMENT_HEIGHT + "]");
        }

        return manager.getFragement(fileName, x, y, width, height);
    }

    public boolean deleteCharta(String fileName) throws FileNotFoundException {
        return manager.deleteFile(fileName);
    }

    public byte[] getWholeCharta(String fileId)
            throws FileNotFoundException {
        return manager.readFileAsBytes(fileId);
    }

}
