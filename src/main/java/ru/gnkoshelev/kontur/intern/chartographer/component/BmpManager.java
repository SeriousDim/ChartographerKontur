package ru.gnkoshelev.kontur.intern.chartographer.component;

import ru.gnkoshelev.kontur.intern.chartographer.config.*;
import ru.gnkoshelev.kontur.intern.chartographer.exception.*;
import ru.gnkoshelev.kontur.intern.chartographer.helpers.Log;
import ru.gnkoshelev.kontur.intern.chartographer.universal.*;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gnkoshelev.kontur.intern.chartographer.universal.MathManager;

import javax.annotation.PostConstruct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Color;

/**
 * Бин для работы с изображениями и директориями.
 * Взаимодействует с файловой системой. Создает, сохраняет,
 * загружает, удаляет и меняет изображения.
 */
@Component(BmpManager.BEAN_NAME)
@Scope("singleton")
public class BmpManager /*extends FileManager*/ {

    public static final String BEAN_NAME = "bmpWorker";
    public static final String FILE_FORMAT = ".bmp";

    @Value("#{mainConfig.bmpPath}")
    protected String path;

    // Методы для работы с бином

    public static BmpManager getAsBean() {
        return AppContextProvider.getBean(BEAN_NAME, BmpManager.class);
    }

    @PostConstruct
    public void postConstruct()
            throws DirectoryCreationFailureException {
        //this.fileFormat = FILE_FORMAT;
        tryCreateDirectory();
    }

    // ...

    public String generateId() {
        var id = "";
        File file = null;

        do {
            id = IdGenerator.generateIdForHuman(MainConfig.UNIQUE_ID_SYMBOLS_LENGTH);
            file = new File(getFilePath(id));
        } while (file.exists());

        return id;
    }

    public byte[] readFileAsBytes(String fileName)
            throws FileNotFoundException {
        var path = getFilePath(fileName);
        var img = IJ.openAsByteBuffer(path);

        if (img == null) {
            throw new FileNotFoundException(path);
        }

        return img.array();
    }

    public String getFilePath(String fileName) {
        return getPath() + "/" + fileName + FILE_FORMAT;
    }

    public String getPath() {
        return path;
    }

    // Собственные методы класса
    // Проверка граничных значений

    public boolean isFragmentWidthCorrect(int width) {
        return MathManager.isBetween(width, 1, MainConfig.MAX_FRAGMENT_WIDTH);
    }

    public boolean isFragmentHeightCorrect(int height) {
        return MathManager.isBetween(height, 1, MainConfig.MAX_FRAGMENT_HEIGHT);
    }

    /**
     * Используйте {@link Rectangle#intersection(Rectangle)} или 
     * {@link Rectangle#intersects(Rectangle)}
     */
    @Deprecated
    public boolean isFragmentInBounds(int x, int y, int width, int height,
                                      int imgWidth, int imgHeight) {
        var x2 = x + width;
        var y2 = y + height;
        var checkX = MathManager.isBetween(x, 0, imgWidth - 1)
                     || MathManager.isBetween(x2, 0, imgWidth - 1);
        var checkY = MathManager.isBetween(y, 0, imgHeight - 1)
                     || MathManager.isBetween(y2, 0, imgHeight - 1);

        return checkX && checkY;
    }

    // Методы основного функционала

    public File openFile(String fileName)
            throws FileNotFoundException {
        var path = getFilePath(fileName);
        var file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException(path);
        }

        return file;
    }

    public void createNewFile(String fileName, int width, int height) {
        var plus = createImage(fileName, width, height);
        IJ.save(plus, getFilePath(fileName));
    }

    public void saveFragment(String fileName,
                             byte[] fragmentBytes,
                             int x, int y,
                             int width, int height)
            throws FileNotFoundException, IOException, ParamOutOfBounds {
        var file = openFile(fileName);
        var img = ImageIO.read(file);

        var fragment = ImageIO.read(new ByteArrayInputStream(fragmentBytes));

        if (fragment.getWidth() != width) {
            throw new ParamOutOfBounds("width", "Ширина присылаемого фрагмента и " +
                    "параметр width должны быть равны");
        }
        if (fragment.getHeight() != height) {
            throw new ParamOutOfBounds("height", "Высота присылаемого фрагмента и " +
                    "параметр height должны быть равны");
        }

        var intersec = MathManager.getFragmentIntersection(x, y, width, height, img.getWidth(), img.getHeight());

        if (!intersec.isEmpty()) {
            var intersecInFrag = MathManager.transformRect(intersec, new Point(0, 0), new Point(x, y));
            var croppedFrag = fragment.getSubimage(intersecInFrag.x, intersecInFrag.y,
                    intersecInFrag.width, intersecInFrag.height);
            var graphics = img.createGraphics();
            graphics.drawImage(croppedFrag, intersec.x, intersec.y, null);
            graphics.dispose();
            ImageIO.write(img, FILE_FORMAT.substring(1), file);
        } else {
            throw new ParamOutOfBounds("x, y, width, height", "Фрагмент должен " +
                    "перескаться с изображением");
        }
    }

    public byte[] getFragement(String fileName,
                               int x, int y,
                               int width, int height)
            throws FileNotFoundException, ParamOutOfBounds, IOException {
        var file = openFile(fileName);
        var img = ImageIO.read(file);

        var usefulFrag = readCropped(file, x, y, width, height);
        var result = createImage("buffer", width, height);

        var intersec = MathManager.getFragmentIntersection(x, y, width, height, img.getWidth(), img.getHeight());
        var point = MathManager.transformPoint(new Point(intersec.x, intersec.y), new Point(0, 0), new Point(x, y));

        var resultImg = result.getBufferedImage();
        var graphics = resultImg.createGraphics();
        graphics.drawImage(usefulFrag, point.x, point.y, null);
        graphics.dispose();

        return bufferedImgToBytes(resultImg);
    }

    public boolean deleteFile(String fileName)
            throws FileNotFoundException {
        var file = openFile(fileName);

        return file.delete();
    }


    // Дополнительные методы класса

    public byte[] bufferedImgToBytes(BufferedImage img)
            throws IOException {
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

    public void tryCreateDirectory()
            throws DirectoryCreationFailureException {
        var logger = Log.get(BmpManager.class);

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
     * <p>
     * Если необходима проверка параметров x, y, width, height, то
     * использойте метод {@link BmpManager#getFragement(String, int, int, int, int)}.
     * <p>
     * Если x, y, width, height выходят за пределы изображения, то
     * обрезанный фрагмент будет получен по границам изображения.
     * Соответственно, размеры фрагмента в таком случае получатся
     * меньше ожидаемых, либо фрагмент получится соверешенно не тем.
     */
    public BufferedImage readCropped(File f, int x, int y,
                                     int width, int height)
            throws IOException {
        var img = ImageIO.read(f);
        var imgWidth = img.getWidth();
        var imgHeight = img.getHeight();
        var sourceRegion = MathManager.getFragmentIntersection(x, y, width, height, imgWidth, imgHeight);
        var stream = ImageIO.createImageInputStream(f);
        var readers = ImageIO.getImageReaders(stream);

        if (!sourceRegion.isEmpty() && readers.hasNext()) {
            ImageReader reader = readers.next();
            reader.setInput(stream);

            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceRegion(sourceRegion);

            var image = reader.read(0, param);
            return image;
        } else {
            var mes = String.format("Не удалось обрезать изображение. Размеры изображения: %dx%d. Попытка вырезать " +
                            "следующий прямоугольник: x = %d, y = %d, ширина = %d, высота = %d.\n" +
                            "Проверьте, что все значения больше нуля", imgWidth, imgHeight,
                    sourceRegion.x, sourceRegion.y,
                    sourceRegion.width, sourceRegion.height);
            throw new IOException(mes);
        }
    }

    public BufferedImage readCropped(File f, Rectangle frag)
            throws IOException {
        return readCropped(f, frag.x, frag.y, frag.width, frag.height);
    }

}
