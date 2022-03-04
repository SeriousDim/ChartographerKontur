package ru.gnkoshelev.kontur.intern.chartographer.universal.interfaces;

import ij.IJ;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
import ru.gnkoshelev.kontur.intern.chartographer.exception.FileNotFoundException;

import java.io.IOException;

public interface FileManagerInterface {

    String generateId();

    byte[] readFileAsBytes(String fileName) throws FileNotFoundException;

    String getFilePath(String fileName);

    String getPath();

    void setPath(String path);

    void createNewFile(String fileName);

}
