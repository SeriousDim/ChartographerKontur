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
import java.awt.image.ImageObserver;
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

        /*var answer = checkX && checkY;
        if (!answer) {
            checkX = isBetween(0, x, x2) || isBetween(imgWidth - 1, x, x2);
            checkY = isBetween(0, y, y2) || isBetween(imgHeight - 1, y, y2);

            return checkX && checkY;
        }*/

        return checkX && checkY;
    }


    // Методы основного функционала

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
        var path = getFilePath(fileName);
        var file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException(fileName);
        }

        var img = ImageIO.read(file);

        if (!isFragmentInBounds(x, y, width, height, img.getWidth(), img.getHeight())) {
            throw new ParamOutOfBounds("x, y",
                    "Фрагмент должен перескаться с изображением (хотя бы одна из вершин фрагмента должна лежать внутри изображения). " +
                            "Размеры изображения: " + img.getWidth() + "x" + img.getHeight());
        }

        var usefulFrag = crop(file, x, y, width, height);
        var result = createImage("buffer", width, height);

        var fragCenter = new Point(x + width/2, y + height/2);
        var imgCenter = new Point(img.getWidth()/2, img.getHeight()/2);
        var translateVec = new Point(imgCenter.x - fragCenter.x, imgCenter.y - fragCenter.y);

        var translateXPos = translateVec.x >= 0;
        var translateYPos = translateVec.y >= 0;

        var point = new Point(0, 0);
        if (translateXPos && translateYPos) {
            point.setLocation(width - usefulFrag.getWidth(), height - usefulFrag.getHeight());
        } else if (translateXPos && !translateYPos) {
            point.setLocation(width - usefulFrag.getWidth(), 0);
        } else if (!translateXPos && translateYPos) {
            point.setLocation(0, height - usefulFrag.getHeight());
        }

        var resultImg = result.getBufferedImage();
        var graphics = resultImg.createGraphics();
        graphics.drawImage(usefulFrag, point.x, point.y, null);
        graphics.dispose();

        return bufferedImgToBytes(resultImg);
    }

    public boolean deleteFile(String fileName)
        throws FileNotFoundException {
        var path = getFilePath(fileName);
        var file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException(fileName);
        }

        return file.delete();
    }


    // Дополнительные методы класса

    public byte[] bufferedImgToBytes(BufferedImage img) throws IOException {
        var baos = new ByteArrayOutputStream();
        try (baos) {
            ImageIO.write(img, FILE_FORMAT.substring(1), baos);
        } catch (Exception e) {
            throw e;
        }

        return baos.toByteArray();
    }

    public ImagePlus createImage(String fileName, int width, int height) {
        var plus = IJ.createImage(fileName, "RGB", width, height, 1);
        var processor = plus.getProcessor();
        drawBlackRect(processor, 0, 0, width, height);
        return plus;
    }

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
    public BufferedImage crop(File f, int x, int y,
                        int width, int height) throws IOException {
        var img = ImageIO.read(f);
        var imgWidth = img.getWidth();
        var imgHeight = img.getHeight();
        var nx = Math.min(Math.max(x, 0), imgWidth-1);
        var ny = Math.min(Math.max(y, 0), imgHeight-1);
        var nx2 = Math.min(Math.max(x+width, 0), imgWidth-1);
        var ny2 = Math.min(Math.max(y+height, 0), imgHeight-1);

        Rectangle sourceRegion = new Rectangle(nx, ny, nx2 - nx + 1, ny2 - ny + 1);

        ImageInputStream stream = ImageIO.createImageInputStream(f);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

        if (readers.hasNext()) {
            ImageReader reader = readers.next();
            reader.setInput(stream);

            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceRegion(sourceRegion);

            var image = reader.read(0, param);
            return image;
        } else {
            var mes = String.format("Не удалось обрезать изображение. Размеры изображения: %dx%d. Попытка обрезать " +
                    "по следующим точкам: (%d, %d), (%d, %d). Ширина: %d. Высота: %d", imgWidth, imgHeight,
                    nx, ny, nx2, ny2, nx2 - nx + 1, ny2 - ny + 1);
            throw new IOException(mes);
        }
    }

}
