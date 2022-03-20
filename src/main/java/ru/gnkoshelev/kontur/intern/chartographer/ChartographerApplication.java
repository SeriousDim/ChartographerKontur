/*
 * (c) 2022, Дмитрий Лыков
 *
 * Больше информации в файле LICENSE
 */
package ru.gnkoshelev.kontur.intern.chartographer;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import ru.gnkoshelev.kontur.intern.chartographer.component.BmpManager;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
import ru.gnkoshelev.kontur.intern.chartographer.helpers.Log;
import ru.gnkoshelev.kontur.intern.chartographer.universal.DirectoryManager;

@SpringBootApplication
public class ChartographerApplication {

    public static void main(String[] args) {
        var logger = Log.get("ChartographerApplication");

        logger.debug("Java version: " + System.getProperty("java.version"));

        // Сначала меняем конфигурацию, только потом запускаем Spring
        // (и инициализируем бины)
        logger.debug("MainConfig.bmpPath: " + MainConfig.bmpPath);
        if (args.length > 0) {
            MainConfig.bmpPath = DirectoryManager.removeLeadSlash(args[0]);
        }
        logger.debug("New value of MainConfig.bmpPath: " + MainConfig.bmpPath);

        SpringApplicationBuilder builder = new SpringApplicationBuilder(ChartographerApplication.class);
        builder.headless(false);

        ConfigurableApplicationContext context = builder.run(args);

        var manager = BmpManager.getAsBean();
        logger.debug("Actual path in BmpManager bean: " + manager.getPath());
    }

}
