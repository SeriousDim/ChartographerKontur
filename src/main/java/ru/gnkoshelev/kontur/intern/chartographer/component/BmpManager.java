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
import ru.gnkoshelev.kontur.intern.chartographer.universal.IdGenerator;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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
public class BmpManager {

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

    public String generateId() {
        var id = "";
        File file = null;

        do {
            id = IdGenerator.generateIdForHuman(MainConfig.UNIQUE_ID_SYMBOLS_LENGTH);
            file = new File(getFilePath(id));
        } while (file.exists());

        return id;
    }

    /*
     *
     */
    public byte[] readFileAsBytes(String fileName) throws FileNotFoundException {
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

    public File openFile(String fileName) throws FileNotFoundException {
        var path = getFilePath(fileName);
        var file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException(fileName);
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

        var intersec = getFragmentIntersection(x, y, width, height, img.getWidth(), img.getHeight());

        if (!intersec.isEmpty()) {
            var intersecInFrag = transformRect(intersec, new Point(0, 0), new Point(x, y));
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

        /*if (!isFragmentInBounds(x, y, width, height, img.getWidth(), img.getHeight())) {
            throw new ParamOutOfBounds("x, y",
                    "Фрагмент должен перескаться с изображением (хотя бы одна из вершин фрагмента должна лежать внутри изображения). " +
                            "Размеры изображения: " + img.getWidth() + "x" + img.getHeight());
        }*/

        var usefulFrag = readCropped(file, x, y, width, height);
        var result = createImage("buffer", width, height);

        /*var fragCenter = new Point(x + width/2, y + height/2);
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
        }*/

        var intersec = getFragmentIntersection(x, y, width, height, img.getWidth(), img.getHeight());
        var point = transformPoint(new Point(intersec.x, intersec.y), new Point(0, 0), new Point(x, y));

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
     *
     * @param input - точка для преобразования, заданная
     *              в старой системе координат
     * @param oldOrigin - точка отсчета старой системы координат,
     *      *           заданная в старой системе координат
     * @param newOrigin - точка отсчета новой системы координат,
     *                  заданная в старой системе координат
     *
     * @return точка input. заданная в новой системе координат
     */
    public Point transformPoint(Point input, Point oldOrigin, Point newOrigin) {
        var nx = oldOrigin.x - newOrigin.x + input.x;
        var ny = oldOrigin.y - newOrigin.y + input.y;
        return new Point(nx, ny);
    }

    public Rectangle transformRect(Rectangle input, Point oldOrigin, Point newOrigin) {
        var result = new Rectangle();
        var newLeftTop = transformPoint(new Point(input.x, input.y), oldOrigin, newOrigin);

        result.x = newLeftTop.x;
        result.y = newLeftTop.y;
        result.width = input.width;
        result.height = input.height;

        return result;
    }

    public Rectangle getFragmentIntersection(int x, int y, int width, int height,
                                             int imgWidth, int imgHeight) {
        var rFrag = new Rectangle(x, y, width, height);
        var rImg = new Rectangle(0, 0, imgWidth, imgHeight);
        return rImg.intersection(rFrag);
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
    public BufferedImage readCropped(File f, int x, int y,
                                     int width, int height) throws IOException {
        var img = ImageIO.read(f);
        var imgWidth = img.getWidth();
        var imgHeight = img.getHeight();
        var sourceRegion = getFragmentIntersection(x, y, width, height, imgWidth, imgHeight);
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

    public BufferedImage readCropped(File f, Rectangle frag) throws IOException {
        return readCropped(f, frag.x, frag.y, frag.width, frag.height);
    }

}
