package ru.gnkoshelev.kontur.intern.chartographer;

import ij.IJ;
import ij.ImageJ;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.gnkoshelev.kontur.intern.chartographer.config.MainConfig;
import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryCreationFailureException;
import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryExistsException;
import ru.gnkoshelev.kontur.intern.chartographer.universal.DirectoryManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class MainAPITests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void directoryCreated()
			throws Exception {
		var path = "content/test/folder";
		var dir = new File("content/test");
		if (dir.exists())
			FileUtils.deleteDirectory(dir);

		DirectoryManager.tryCreateDirectory(path);
		dir = new File(path);
		Assert.assertTrue(dir.exists());
		Assert.assertThrows(DirectoryExistsException.class,
				() -> {DirectoryManager.tryCreateDirectory(path);});
		FileUtils.deleteDirectory(new File("content/test"));
	}

	@Test
	void createCharta() throws Exception {
		System.setProperty("java.awt.headless", "false");

		var path = "content/test/folder";
		var route = MainConfig.HEAD_ROUTE;
		DirectoryManager.tryCreateDirectory(path);

		var result = mockMvc.perform(post(route + "/?width=1000&height=1500"))
				.andExpect(status().isCreated())
				.andReturn();

		var id = result.getResponse().getContentAsString();
		Assert.assertFalse(id.isEmpty());

		var result2 = mockMvc.perform(get(route + "/" + id +
				"/?x=10&y=10&width=100&height=200"))
				.andExpect(status().isOk())
				.andReturn();

		Assert.assertEquals("image/bmp",
				result2.getResponse().getContentType());
		var content = result2.getResponse().getContentAsByteArray();
		Assert.assertNotNull(content);
		var img = ImageIO.read(new ByteArrayInputStream(content));
		Assert.assertEquals(img.getWidth(), 100);
		Assert.assertEquals(img.getHeight(), 200);

		var pixel = img.getRGB(0, 0);
		Assert.assertEquals(Color.BLACK.getRGB(), pixel);

		mockMvc.perform(post(route + "/?width=20001&height=1500"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(route + "/?width=1000&height=50001"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(route + "/?width=1000&height=0"))
				.andExpect(status().isBadRequest());

		FileUtils.deleteDirectory(new File("content/test"));
	}

	@Test
	void getFragment() throws Exception {
		System.setProperty("java.awt.headless", "false");

		var path = "content/test/folder";
		var route = MainConfig.HEAD_ROUTE;
		DirectoryManager.tryCreateDirectory(path);

		var result = mockMvc.perform(post(route + "/?width=1000&height=1500"))
				.andExpect(status().isCreated())
				.andReturn();
		var id = result.getResponse().getContentAsString();

		mockMvc.perform(get(route + "/111/?x=10&y=10&width=10&height=20"))
				.andExpect(status().isNotFound());

		mockMvc.perform(get(route + "/" + id +
				"/?x=10&y=10&width=100&height=200"))
				.andExpect(status().isOk());
		var result2 = mockMvc.perform(get(route + "/" + id +
				"/?x=-100&y=-100&width=300&height=300"))
				.andExpect(status().isOk())
				.andReturn();
		mockMvc.perform(get(route + "/" + id +
				"/?x=-100&y=-100&width=1200&height=300"))
				.andExpect(status().isOk());
		mockMvc.perform(get(route + "/" + id +
				"/?x=-100&y=-100&width=30&height=30"))
				.andExpect(status().isBadRequest());
		var content = result2.getResponse().getContentAsByteArray();
		var img = ImageIO.read(new ByteArrayInputStream(content));
		Assert.assertEquals(img.getWidth(), 300);
		Assert.assertEquals(img.getHeight(), 300);

		var pixel = img.getRGB(0, 0);
		Assert.assertEquals(Color.BLACK.getRGB(), pixel);

		FileUtils.deleteDirectory(new File("content/test"));
	}

	@Test
	void saveFragment() throws Exception {
		System.setProperty("java.awt.headless", "false");

		var path = "content/test/folder";
		var route = MainConfig.HEAD_ROUTE;
		DirectoryManager.tryCreateDirectory(path);

		var result = mockMvc.perform(post(route + "/?width=1000&height=1500"))
				.andExpect(status().isCreated())
				.andReturn();
		var id = result.getResponse().getContentAsString();

		var testImg = getTestImage();

		var result4 = mockMvc.perform(post(route + "/" + id +
				"/?x=-100&y=-100&width=200&height=200")
					.content(testImg)
					.contentType("image/bmp"))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();
		var pic = mockMvc.perform(get(route + "/" + id +
				"/?x=0&y=0&width=1000&height=1500"))
				.andExpect(status().isOk())
				.andReturn();
		var content = pic.getResponse().getContentAsByteArray();
		var img = ImageIO.read(new ByteArrayInputStream(content));
		Assert.assertEquals(Color.GREEN.getRGB(), img.getRGB(99, 99));
		Assert.assertEquals(Color.BLACK.getRGB(), img.getRGB(100, 100));

		mockMvc.perform(post(route + "/" + id +
				"/?x=50&y=50&width=200&height=200")
				.content(testImg)
				.contentType("image/bmp"))
				.andExpect(status().isOk());
		pic = mockMvc.perform(get(route + "/" + id +
				"/?x=0&y=0&width=1000&height=1500"))
				.andExpect(status().isOk())
				.andReturn();
		var content3 = pic.getResponse().getContentAsByteArray();
		var img3 = ImageIO.read(new ByteArrayInputStream(content3));
		Assert.assertEquals(Color.RED.getRGB(), img3.getRGB(50, 50));
		Assert.assertEquals(Color.GREEN.getRGB(), img3.getRGB(49, 49));

		FileUtils.deleteDirectory(new File("content/test"));
	}

	byte[] getTestImage() throws Exception {
		var result = IJ.createImage("", "RGB", 200, 200, 1);
		var p = result.getProcessor();

		p.setColor(Color.RED);
		p.fillRect(0, 0, 100, 100);

		p.setColor(Color.YELLOW);
		p.fillRect(100, 0, 100 ,100);

		p.setColor(Color.BLUE);
		p.fillRect(0, 100, 100, 100);

		p.setColor(Color.GREEN);
		p.fillRect(100, 100, 100, 100);

		var buf = p.getBufferedImage();

		var baos = new ByteArrayOutputStream();
		try (baos) {
			ImageIO.write(p.getBufferedImage(), "bmp", baos);
		} catch (Exception e) {
			throw e;
		}

		return baos.toByteArray();
	}

}
