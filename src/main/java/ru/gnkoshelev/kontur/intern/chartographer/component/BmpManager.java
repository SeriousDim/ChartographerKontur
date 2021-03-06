/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
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
 * Менеджер файлов для работы с изображениями.
 * Взаимодействует с файловой системой. Создает, сохраняет,
 * загружает, удаляет и меняет изображения.
 *
 * Для работы необходимо задать директорию в поле {@link BmpManager#path},
 * с которой будет работать данный менеджер файлов.
 *
 * Также можно задать {@link BmpManager#FILE_FORMAT}
 */
@Component(BmpManager.BEAN_NAME)
@Scope("singleton")
public class BmpManager {

    public static final String BEAN_NAME = "bmpWorker";
    public static final String FILE_FORMAT = ".bmp";

    /**
     * Директория, с которой работает данный менеджер.
     * Можно задать в классе-конфигурации {@link MainConfig}
     * Получить значение можно с помощью {@link BmpManager#getPath()}
     */
    @Value("#{mainConfig.bmpPath}")
    private String path;

    private Logger logger;


    // Методы для работы с бином

    /**
     * Возвращает бин класса {@link BmpManager}
     * @return {@link BmpManager}
     */
    public static BmpManager getAsBean() {
        return AppContextProvider.getBean(BEAN_NAME, BmpManager.class);
    }

    @PostConstruct
    public void postConstruct() throws DirectoryCreationFailureException {
        tryCreateDirectory();
    }

    /**
     * Сгенерировать уникальный id для нового файла.
     * @return id в виде строки
     */
    public String generateId() {
        var id = "";
        File file = null;

        do {
            id = IdGenerator.generateIdForHuman(MainConfig.UNIQUE_ID_SYMBOLS_LENGTH);
            file = new File(getFilePath(id));
        } while (file.exists());

        return id;
    }

    /**
     * Прочитать файл с данным именем из директории {@link BmpManager#path}
     * @param fileName имя файла, который лежит в директории {@link BmpManager#path}
     * @return прочитанный массив байтов
     * @throws FileNotFoundException
     */
    public byte[] readFileAsBytes(String fileName) throws FileNotFoundException {
        var path = getFilePath(fileName);
        var img = IJ.openAsByteBuffer(path);

        if (img == null) {
            throw new FileNotFoundException(path);
        }

        return img.array();
    }

    /**
     * Возвращает путь к данному файлу. Директория задается
     * полем {@link BmpManager#path}
     * @param fileName имя файла
     * @return путь к файлу в виде some/path/to/file.bmp
     */
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


    // Методы основного функционала

    public File openFile(String fileName) throws FileNotFoundException {
        var path = getFilePath(fileName);
        var file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException(fileName);
        }

        return file;
    }

    /**
     * Создает новый файл с изображением и сохраняет его
     * @param fileName название файла
     * @param width ширина изображения
     * @param height высота изображения
     */
    public void createNewFile(String fileName, int width, int height) {
        var plus = createImage(fileName, width, height);
        IJ.save(plus, getFilePath(fileName));
    }

    /**
     * Заменяет заданный фрагмент изображения на новый.
     * Обратите внимание, что width и height должны быть равны ширине и
     * высоте передаваемого в аргументе {@code fragmentBytes} фрагмента
     * соответсвенно.
     * @param fileName название файла с изображением, у которого надо заменить
     *                 определенный фрагмент
     * @param fragmentBytes фрагмент в виде массива битов, который будет
     *                      вставлен в изображение
     * @param x координата x левой верхней точки области изображения, которая
     *          будет заменена на фрагмент
     * @param y координата y левой верхней точки области изображения, которая
     *          будет заменена на фрагмент
     * @param width ширина заменяемого фрагмента
     * @param height высота заменяемого фрагмента
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParamOutOfBounds при любой ошибке, связанной с параметрами
     * метода
     */
    public void saveFragment(String fileName,
                             byte[] fragmentBytes,
                             int x, int y,
                             int width, int height)
            throws FileNotFoundException, IOException, ParamOutOfBounds {
        var file = openFile(fileName);
        var img = ImageIO.read(file);

        var fragment = ImageIO.read(new ByteArrayInputStream(fragmentBytes));

        if (fragment.getWidth() != width) {
            throw new ParamOutOfBounds("width",
                    "Ширина присылаемого фрагмента и " +
                    "параметр width должны быть равны");
        }
        if (fragment.getHeight() != height) {
            throw new ParamOutOfBounds("height",
                    "Высота присылаемого фрагмента и " +
                    "параметр height должны быть равны");
        }

        var intersec = MathManager.getFragmentIntersection(x, y, width, height,
                img.getWidth(), img.getHeight());

        if (!intersec.isEmpty()) {
            var intersecInFrag = MathManager.transformRect(intersec,
                    new Point(0, 0), new Point(x, y));
            var croppedFrag = fragment.getSubimage(intersecInFrag.x,
                    intersecInFrag.y, intersecInFrag.width,
                    intersecInFrag.height);
            var graphics = img.createGraphics();
            graphics.drawImage(croppedFrag,
                    intersec.x, intersec.y, null);
            graphics.dispose();
            ImageIO.write(img, FILE_FORMAT.substring(1), file);
        } else {
            throw new ParamOutOfBounds("x, y, width, height",
                    "Фрагмент должен " +
                    "перескаться с изображением");
        }
    }

    /**
     * Возвращает фрагмент изображения
     * @param fileName имя файла с изображением
     * @param x координата x левой верхней точки фрагмента
     * @param y координата y левой верхней точки фрагмента
     * @param width ширина фрагмента
     * @param height высота фрагмента
     * @return
     * @throws FileNotFoundException
     * @throws ParamOutOfBounds при любой ошибке, связанной с параметрами
     * метода
     * @throws IOException
     */
    public byte[] getFragement(String fileName,
                               int x, int y,
                               int width, int height)
            throws FileNotFoundException, ParamOutOfBounds, IOException {
        var file = openFile(fileName);
        var img = ImageIO.read(file);

        var usefulFrag = readCropped(file, x, y, width, height);
        var result = createImage("buffer", width, height);

        var intersec = MathManager.getFragmentIntersection(x, y, width, height,
                img.getWidth(), img.getHeight());
        var point = MathManager.transformPoint(new Point(intersec.x, intersec.y),
                new Point(0, 0), new Point(x, y));

        var resultImg = result.getBufferedImage();
        var graphics = resultImg.createGraphics();
        graphics.drawImage(usefulFrag, point.x, point.y, null);
        graphics.dispose();

        return bufferedImgToBytes(resultImg);
    }

    /**
     * Удаляет файл с данным именем из директории {@link BmpManager#path}
     * @param fileName имя файла
     * @return true, в случае успешного удаления; false - иначе
     * @throws FileNotFoundException
     */
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

    /**
     * Создать новый объект изображения {@link ImagePlus}
     * @param fileName имя изображения
     * @param width ширина изображения
     * @param height высота изображения
     * @return объект {@link ImagePlus}, представляющий изображение
     */
    public ImagePlus createImage(String fileName, int width, int height) {
        var plus = IJ.createImage(fileName, "RGB", width, height, 1);
        var processor = plus.getProcessor();
        drawBlackRect(processor, 0, 0, width, height);
        return plus;
    }

    /**
     * @see DirectoryManager#tryCreateDirectory(String)
     * @throws DirectoryCreationFailureException
     */
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
     * Возращает максимально возможный обрезанный фрагмент. Этот метод
     * не читает все изображение, а читает только нужный его фрагмент.
     *
     * Если x, y, width, height выходят за пределы изображения, то
     * обрезанный фрагмент будет получен по границам изображения.
     * Соответственно, размеры фрагмента в таком случае получатся
     * меньше ожидаемых, либо фрагмент получится соверешенно не тем.
     *
     * Если вам нужно получить фрагмент имеено с данными шириной и высотой,
     * то использойте метод
     * @link BmpManager#getFragement(String, int, int, int, int)}.
     * @param f файл с изображением, который надо обрезать
     * @param x координата x левой верхней точки фрагмента
     * @param y координата y левой верхней точки фрагмента
     * @param width ожидаемая ширина обрезанного фрагмента
     * @param height ожидаемая высота обрезанного фрагмента
     * @return
     * @throws IOException
     * @throws ParamOutOfBounds
     */
    public BufferedImage readCropped(File f, int x, int y,
                                     int width, int height)
            throws IOException, ParamOutOfBounds {
        var img = ImageIO.read(f);
        var imgWidth = img.getWidth();
        var imgHeight = img.getHeight();
        var sourceRegion = MathManager.getFragmentIntersection(x, y, width,
                height, imgWidth, imgHeight);
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
            var mes = String.format(MainConfig.CANNOT_CROP_IMAGE_MESSAGE,
                    imgWidth, imgHeight,
                    sourceRegion.x, sourceRegion.y,
                    sourceRegion.width, sourceRegion.height);
            throw new ParamOutOfBounds("x, y, width, height",
                    "Фрагмент должен " +
                            "перескаться с изображением");
        }
    }

    /**
     * @see BmpManager#readCropped(File, int, int, int, int)
     * @param f
     * @param frag
     * @return
     * @throws IOException
     * @throws ParamOutOfBounds
     */
    public BufferedImage readCropped(File f, Rectangle frag)
            throws IOException, ParamOutOfBounds {
        return readCropped(f, frag.x, frag.y, frag.width, frag.height);
    }

}