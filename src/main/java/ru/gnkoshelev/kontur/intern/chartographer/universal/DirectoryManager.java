package ru.gnkoshelev.kontur.intern.chartographer.universal;

import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryCreationFailureException;
import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryExistsException;

import java.io.File;
import java.util.regex.Pattern;

public class DirectoryManager {

    @Deprecated
    public static boolean isValidPath(String path) {
        return !path.isBlank() &&
                    (path.contains("/") ||
                        path.contains("\\"));
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

}
