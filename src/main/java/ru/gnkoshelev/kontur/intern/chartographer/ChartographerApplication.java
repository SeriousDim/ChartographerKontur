package ru.gnkoshelev.kontur.intern.chartographer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChartographerApplication {

	public static void main(String[] args) {
		System.out.println("Java version:" + System.getProperty("java.version"));
		SpringApplication.run(ChartographerApplication.class, args);
	}

}
