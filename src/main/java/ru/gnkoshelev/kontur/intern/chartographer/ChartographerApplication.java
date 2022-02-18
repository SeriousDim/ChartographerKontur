package ru.gnkoshelev.kontur.intern.chartographer;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import ru.gnkoshelev.kontur.intern.chartographer.config.Log;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
import ru.gnkoshelev.kontur.intern.chartographer.universal.BmpManager;
import ru.gnkoshelev.kontur.intern.chartographer.universal.DirectoryManager;

@SpringBootApplication
public class ChartographerApplication {

    @Autowired
    public static BmpManager worker;

    private static Logger logger;

    public static void main(String[] args) {
        logger = Log.get("ChartographerApplication");

        logger.debug("Java version: " + System.getProperty("java.version"));

        // Сначала меняем конфигурацию, только потом запускаем Spring
        // (и инициализируем бины)
        logger.debug("MainConfig.bmpPath: " + MainConfig.bmpPath);
        if (args.length > 0) {
            var pathName = DirectoryManager.removeLeadSlash(args[0]);

            MainConfig.bmpPath = pathName;
        }
        logger.debug("New value of MainConfig.bmpPath: " + MainConfig.bmpPath);

        //SpringApplication.run(ChartographerApplication.class, args);
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ChartographerApplication.class);

        builder.headless(false);

        ConfigurableApplicationContext context = builder.run(args);

        var manager = BmpManager.getAsBean();
        logger.debug("Actual path in BmpManager bean: " + manager.getPath());
    }

}
