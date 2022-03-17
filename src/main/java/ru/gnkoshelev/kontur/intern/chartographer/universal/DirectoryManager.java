package ru.gnkoshelev.kontur.intern.chartographer.universal;

import ru.gnkoshelev.kontur.intern.chartographer.exception.*;

import java.io.File;

public class DirectoryManager {

    public static String removeLeadSlash(String path) {
        if (path.startsWith("\\\\")) {
            return path.substring(2);
        } else if (path.startsWith("/") ||
                path.startsWith("\\")) {
            return path.substring(1);
        }

        return path;
    }

    public static void tryCreateDirectory(String path)
            throws DirectoryCreationFailureException, DirectoryExistsException {
        var dir = new File(path);

        if (dir.exists()) {
            throw new DirectoryExistsException(path);
        }

        var result = dir.mkdirs();
        if (!result) {
            throw new DirectoryCreationFailureException(path);
        }
    }

    /**
     * Используйте {@link DirectoryManager#tryCreateDirectory(String)}
     */
    @Deprecated
    public static boolean isValidPath(String path) {
        return !path.isBlank() &&
                (path.contains("/") ||
                        path.contains("\\"));
    }

}
