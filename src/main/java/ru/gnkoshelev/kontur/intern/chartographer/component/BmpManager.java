package ru.gnkoshelev.kontur.intern.chartographer.component;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
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
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;


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
        tryCreateDirectory();
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
    // Проверка граничных значений

    public boolean isBetween(int value, int start, int end) {
        return value >= start && value <= end;
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
        var checkX = isBetween(x, 0, imgWidth - 1) || isBetween(x2, 0, imgWidth - 1);
        var checkY = isBetween(y, 0, imgHeight - 1) || isBetween(y2, 0, imgHeight - 1);

        return checkX && checkY;
    }


    // Методы основного функционала

    public ImagePlus createImage(String fileName, int width, int height) {
        var plus = createImage(fileName, width, height);
        var processor = plus.getProcessor();
        drawBlackRect(processor, 0, 0, width, height);
        return plus;
    }

    public void createNewFile(String fileName, int width, int height) {
        var plus = createImage(fileName, width, height);
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
                    "[0, " + MainConfig.MAX_FRAGMENT_WIDTH + "]");
        }
        if (!isFragmentHeightCorrect(height)) {
            throw new ParamOutOfBounds("height",
                    "[0, " + MainConfig.MAX_FRAGMENT_HEIGHT + "]");
        }

        var path = getFilePath(fileName);
        var file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException(fileName);
        }

        var img = ImageIO.read(file);

        if (!isFragmentInBounds(x, y, width, height, img.getWidth(), img.getHeight())) {
            throw new ParamOutOfBounds("x, y",
                    "Фрагмент должен перескаться с изображением. " +
                            "Размеры изображения: " + img.getWidth() + "x" + img.getHeight());
        }

        var usefulFrag = crop(img, x, y, width, height);
        var result = createImage("buffer", width, height);

        var fragCenter = new int[] {x + width/2, y + height/2};
        var imgCenter = new int[] {img.getWidth()/2, img.getHeight()/2};
        var translateVec = new int[] {imgCenter[0] - fragCenter[0], imgCenter[1] - fragCenter[1]};


    }

    public void deleteFile(String fileName) {

    }


    // Дополнительные методы класса

    public void tryCreateDirectory() throws DirectoryCreationFailureException {
        logger = Log.get(BmpManager.class);

        try {
            DirectoryManager.tryCreateDirectory(getPath());
        } catch (DirectoryExistsException e) {
            logger.info(e.getClass().getSimpleName() + " : " + e.getMessage());
        } catch (DirectoryCreationFailureException e) {
            throw e;
        }
    }

    public void drawBlackRect(ImageProcessor p, int x, int y,
                               int width, int height) {
        p.setColor(Color.BLACK);
        p.fill();
        p.drawRect(x, y, width, height);
    }

    /**
     * Возращает максимально возможный обрезанный фрагмент по границам
     * изображения без проверки параметров.
     *
     * Если необходима проверка параметров x, y, width, height, то
     * использойте метод {@link BmpManager#getFragement(String, int, int, int, int)}.
     *
     * Если x, y, width, height выходят за пределы изображения, то
     * обрезанный фрагмент будет получен по границам изображения.
     * Соответственно, размеры фрагмента в таком случае получатся
     * меньше ожидаемых, либо фрагмент получится соверешенно не тем.
     */
    public byte[] crop(BufferedImage img, int x, int y,
                        int width, int height) throws IOException {
        var imgWidth = img.getWidth();
        var imgHeight = img.getHeight();
        var nx = Math.min(Math.max(x, 0), imgWidth-1);
        var ny = Math.min(Math.max(y, 0), imgHeight-1);
        var nx2 = Math.min(Math.max(x+width, 0), imgWidth-1);
        var ny2 = Math.min(Math.max(y+height, 0), imgHeight-1);

        /*Rectangle sourceRegion = new Rectangle(nx, ny, nx2 - nx + 1, ny2 - ny + 1);

        ImageInputStream stream = ImageIO.createImageInputStream(img);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

        if (readers.hasNext()) {
            ImageReader reader = readers.next();
            reader.setInput(stream);

            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceRegion(sourceRegion); // Set region

            BufferedImage image = reader.read(0, param);
        }*/

        var sub = img.getSubimage(nx, ny, nx2 - nx + 1, ny2 - ny + 1);
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
