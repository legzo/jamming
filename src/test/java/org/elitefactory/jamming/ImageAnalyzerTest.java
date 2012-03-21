package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import junit.framework.Assert;

import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.elitefactory.jamming.model.TrafficHistory;
import org.elitefactory.jamming.model.TrafficState;
import org.elitefactory.jamming.model.TrafficStatus;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class ImageAnalyzerTest {

	private static final Logger logger = LoggerFactory.getLogger(ImageAnalyzerTest.class);

	@Test
	public void shouldReadPixelColorsCorrectly() throws Exception {
		BufferedImage image = getImage();

		Assert.assertEquals(TrafficStatus.unknown, ImageAnalyzer.getStatusFromPixel(image, 0, 0));
		Assert.assertEquals(TrafficStatus.normal, ImageAnalyzer.getStatusFromPixel(image, 105, 213));
		Assert.assertEquals(TrafficStatus.slow, ImageAnalyzer.getStatusFromPixel(image, 110, 192));
		Assert.assertEquals(TrafficStatus.slow, ImageAnalyzer.getStatusFromPixel(image, 146, 309));
		Assert.assertEquals(TrafficStatus.stopped, ImageAnalyzer.getStatusFromPixel(image, 121, 173));
	}

	private BufferedImage getImage() throws IOException {
		BufferedImage image = ImageIO.read(new File("src/test/resources/bison-color.png"));
		return image;
	}

	public void printPixels() throws IOException {
		BufferedImage image = getImage();

		printPixelARGB(image.getRGB(0, 0));
		printPixelARGB(image.getRGB(105, 213)); // Green
		printPixelARGB(image.getRGB(110, 192)); // Orange
		printPixelARGB(image.getRGB(146, 309)); // Orange
		printPixelARGB(image.getRGB(121, 173)); // Red
	}

	public void printPixelARGB(int pixel) {
		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;
		logger.debug("R" + red + ", G" + green + ", B" + blue);
	}

	// @Test
	public void testGetImages() throws Exception {
		ImageGetter.fetchBisonImages();
	}

	@Test
	public void shouldReturnCurrentState() throws Exception {
		ImageAnalyzer analyzer = new ImageAnalyzer();
		TrafficState currentState = analyzer.getCurrentState();

		Assert.assertNotNull(currentState);
		Assert.assertEquals(31, currentState.getState().values().size());
		logger.debug("{}", analyzer.getCurrentStateAsJSON());
	}

	// @Test
	public void testAnalyseImage() throws Exception {
		ImageAnalyzer analyzer = new ImageAnalyzer();
		TrafficHistory history = new TrafficHistory();

		File imagesFolder = new File("target/images");

		String[] imageFiles = imagesFolder.list();
		Date start = new Date();
		for (String imageFilePath : imageFiles) {
			if (imageFilePath.endsWith(".png")) {

				BufferedImage image = ImageIO.read(new File(imagesFolder, imageFilePath));
				SimpleDateFormat sdf = new SimpleDateFormat("'bison'-MM-dd-HH_mm'.png'");
				Date parsedDate = sdf.parse(imageFilePath);

				parsedDate = DateUtils.setYears(parsedDate, 2012);

				TrafficState currentState = analyzer.getCurrentStateFromImage(image);
				currentState.setTime(parsedDate);

				history.putState(currentState);

				logger.debug("{}", currentState);
			}
		}
		logger.info("{} files treated in {}ms", history.getNumberOfSamples(), new Date().getTime() - start.getTime());
		logger.info("Max was : {}", history.getMax());

		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File("target/history.json"), history);
		Assert.assertEquals(156, history.getNumberOfSamples());
	}
}
