package ru.gnkoshelev.kontur.intern.chartographer;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.Assert;
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
import ru.gnkoshelev.kontur.intern.chartographer.exception.DirectoryExistsException;
import ru.gnkoshelev.kontur.intern.chartographer.universal.DirectoryManager;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

	/*@Test
	void fullUXCycle() {

	}

	@Test
	void createCharta() throws Exception {
		var path = "content/test/folder";
		var route = MainConfig.HEAD_ROUTE;
		DirectoryManager.tryCreateDirectory(path);

		var result = mockMvc.perform(post(route + "/?width=1000&height=1500"))
				.andExpect(status().isOk())
				.andReturn();

		var id = result.getResponse().getContentAsString();
		Assert.assertFalse(id.isEmpty());

		var result2 = mockMvc.perform(get(route + "/" + id +
				"/?x=10&y=10&width=100&height=200"))
				.andExpect(status().isOk())
				.andReturn();

		Assert.assertEquals(result2.getResponse().getContentType(),
				"image/bmp");
		var content = result2.getResponse().getContentAsByteArray();
		Assert.assertNotNull(content);
		var img = ImageIO.read(new ByteArrayInputStream(content));
		Assert.assertEquals(img.getWidth(), 100);
		Assert.assertEquals(img.getHeight(), 200);

		var pixel = img.getRGB(0, 0);
		Assert.assertEquals(pixel, 0);

		FileUtils.deleteDirectory(new File("content/test"));
	}

	@Test
	void getFragment() {

	}

	@Test
	void saveFragment() {

	}

	@Test
	void deleteCharta() {

	}*/

}
