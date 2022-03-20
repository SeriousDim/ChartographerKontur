package ru.gnkoshelev.kontur.intern.chartographer.universal;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextResourceManager {

    public static String getText(String fileName) throws IOException {
        var path = "classpath:" + fileName;
        var res = TextResourceManager.class.getClassLoader().getResourceAsStream(path);
        var value = new String(res.readAllBytes(), StandardCharsets.UTF_8);
        return value;
    }

}
