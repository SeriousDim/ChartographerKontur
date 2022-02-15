package ru.gnkoshelev.kontur.intern.chartographer.universal;

import java.io.File;

public class FileManager {

    public boolean isValidPath(String path) {
        return !path.isBlank() &&
                    (path.contains("/") ||
                        path.contains("\\"));
    }

    public boolean createDirectoryIfNotExists(String path) {
        if (!isValidPath(path)) {
            return false;
        }

        var dir = new File(path);
        return dir.mkdirs();
    }

    public void createNewFile(String path, String fileName) {

    }

    public void deleteFile(String path, String fileName) {

    }

}
