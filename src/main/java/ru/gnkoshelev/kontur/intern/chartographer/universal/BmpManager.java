package ru.gnkoshelev.kontur.intern.chartographer.universal;

import ij.IJ;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Component;
import ru.gnkoshelev.kontur.intern.chartographer.config.AppContextProvider;
import ru.gnkoshelev.kontur.intern.chartographer.config.Log;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryCreationFailureException;
import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryExistsException;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Бин для работы с изображениями и директориями.
 * Взаимодействует с файловой системой. Создает, сохраняет,
 * загружает, удаляет и меняет изображения.
 */
@Component(BmpManager.BEAN_NAME)
@Scope("singleton")
public class BmpManager {

    public static final String BEAN_NAME = "bmpWorker";

    public static final String FILE_FORMAT = ".bmp";


    @Value("#{mainConfig.bmpPath}")
    private String path;

    private Logger logger;


    public static BmpManager getAsBean() {
        return AppContextProvider.getBean(BEAN_NAME, BmpManager.class);
    }

    @PostConstruct
    public void postConstruct() throws DirectoryCreationFailureException {
        logger = Log.get(BmpManager.class);

        try {
            DirectoryManager.tryCreateDirectory(getPath());
        } catch (DirectoryExistsException e) {
            logger.info(e.getClass().getSimpleName() + " : " + e.getMessage());
        } catch (DirectoryCreationFailureException e) {
            throw new DirectoryCreationFailureException(e.getMessage());
        }
    }


    public String generateId() {
        return "1";
    }

    public void createNewFile(String fileName, int width, int height) {
        var plus = IJ.createImage(fileName, "RGB", width, height, 1);
        IJ.save(plus, getFilePath(fileName));

    }

    public void createNewFile(String fileName) {
        createNewFile(fileName, MainConfig.MAX_WIDTH, MainConfig.MAX_HEIGHT);
    }

    public void saveFragment (String fileName, File bmp) {

    }

    public void getFragement (String fileName) {

    }

    public void deleteFile(String fileName) {

    }

    /**
     * null, если файл не найден
     *
     * @param fileName
     * @return
     */
    public byte[] getFile(String fileName) throws IOException {
        /*var fp = getFilePath(fileName);
        var file = new File(fp);
        return new FileInputStream(file);*/
        var img = IJ.openAsByteBuffer(getFilePath(fileName));
        return img.array();
    }

    public String getFilePath(String fileName) {
        return path + "/" + fileName + FILE_FORMAT;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
