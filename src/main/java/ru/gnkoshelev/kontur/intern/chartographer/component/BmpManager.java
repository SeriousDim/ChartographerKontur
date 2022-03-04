package ru.gnkoshelev.kontur.intern.chartographer.component;

import ij.IJ;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gnkoshelev.kontur.intern.chartographer.config.AppContextProvider;
import ru.gnkoshelev.kontur.intern.chartographer.exception.FileNotFoundException;
import ru.gnkoshelev.kontur.intern.chartographer.exception.ParamOutOfBounds;
import ru.gnkoshelev.kontur.intern.chartographer.helpers.Log;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryCreationFailureException;
import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryExistsException;
import ru.gnkoshelev.kontur.intern.chartographer.universal.DirectoryManager;
import ru.gnkoshelev.kontur.intern.chartographer.universal.interfaces.FileManagerInterface;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


/**
 * Бин для работы с изображениями и директориями.
 * Взаимодействует с файловой системой. Создает, сохраняет,
 * загружает, удаляет и меняет изображения.
 */
@Component(BmpManager.BEAN_NAME)
@Scope("singleton")
public class BmpManager implements FileManagerInterface {

    public static final String BEAN_NAME = "bmpWorker";
    public static final String FILE_FORMAT = ".bmp";


    @Value("#{mainConfig.bmpPath}")
    private String path;

    private Logger logger;


    // Методы для работы с бином

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


    // Реализация методов интерфейса FileManagerInterface

    @Override
    public String generateId() {
        return "4";
    }

    /*
     *
     */
    @Override
    public byte[] readFileAsBytes(String fileName) throws FileNotFoundException {
        var path = getFilePath(fileName);
        var img = IJ.openAsByteBuffer(path);

        if (img == null) {
            throw new FileNotFoundException(path);
        }

        return img.array();
    }

    @Override
    public String getFilePath(String fileName) {
        return path + "/" + fileName + FILE_FORMAT;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void createNewFile(String fileName) {
        createNewFile(fileName, MainConfig.MAX_WIDTH, MainConfig.MAX_HEIGHT);
    }


    // Собственные методы класса

    public boolean isBetween(int value, int left, int right) {
        return value >= left && value <= right;
    }

    public boolean isFragmentWidthCorrect(int width) {
        return isBetween(width, 1, MainConfig.MAX_FRAGMENT_WIDTH);
    }

    public boolean isFragmentHeightCorrect(int height) {
        return isBetween(height, 1, MainConfig.MAX_FRAGMENT_HEIGHT);
    }

    public boolean isFragmentInBounds(int x, int y, int width, int height,
                                      int imgWidth, int imgHeight) {
        var x2 = x + width;
        var y2 = y + height;
        var checkX = isBetween(x, 0, imgWidth) || isBetween(x2, 0, imgWidth);
        var checkY = isBetween(y, 0, imgHeight) || isBetween(y2, 0, imgHeight);

        return checkX && checkY;
    }

    public void createNewFile(String fileName, int width, int height) {
        var plus = IJ.createImage(fileName, "RGB", width, height, 1);
        IJ.save(plus, getFilePath(fileName));
    }

    public void saveFragment(String fileName, File bmp) {

    }

    public byte[] getFragement(String fileName,
                               int x, int y,
                               int width, int height)
            throws FileNotFoundException, ParamOutOfBounds, IOException {
        if (!isFragmentWidthCorrect(width)) {
            throw new ParamOutOfBounds("width",
                    "[1, " + MainConfig.MAX_FRAGMENT_WIDTH + "]");
        }
        if (!isFragmentHeightCorrect(height)) {
            throw new ParamOutOfBounds("height",
                    "[1, " + MainConfig.MAX_FRAGMENT_HEIGHT + "]");
        }

        var path = getFilePath(fileName);
        var file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException(fileName);
        }

        var img = ImageIO.read(file);

        if (!isFragmentInBounds(x, y, width, height, img.getWidth(), img.getHeight())) {
            throw new ParamOutOfBounds("x, y",
                    "положительные целые числа. " +
                            "Фрагмент должен перескаться с изображением. " +
                            "Размеры изображения: " + img.getWidth() + "x" + img.getHeight());
        }

        return crop(img, x, y, width, height);
    }

    public void deleteFile(String fileName) {

    }

    private byte[] crop(BufferedImage img, int x, int y,
                        int width, int height) throws IOException {
        var sub = img.getSubimage(x, y, width, height);
        var baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(sub, FILE_FORMAT.substring(1), baos);
        } catch (Exception e) {
            throw e;
        } finally {
            baos.close();
        }

        return baos.toByteArray();
    }

}
