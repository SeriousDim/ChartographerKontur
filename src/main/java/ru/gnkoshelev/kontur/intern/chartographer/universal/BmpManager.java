package ru.gnkoshelev.kontur.intern.chartographer.universal;

import ij.IJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gnkoshelev.kontur.intern.chartographer.config.AppContextProvider;
import ru.gnkoshelev.kontur.intern.chartographer.config.Log;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryCreationFailureException;
import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryExistsException;

import javax.annotation.PostConstruct;
import java.io.File;


/**
 * Бин для работы с изображениями и директориями.
 * Взаимодействует с файловой системой. Создает, сохраняет,
 * загружает, удаляет и меняет изображения.
 */
@Component(BmpManager.BEAN_NAME)
@Scope("singleton")
public class BmpManager {

    public static final String BEAN_NAME = "bmpWorker";


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
        var plus = IJ.createImage(fileName, "RGB", width, height, 24);
        IJ.save(plus, path + "/" + fileName + ".bmp");

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


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
