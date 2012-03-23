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
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.elitefactory.jamming.model.RocadeDirection;
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

		Assert.assertEquals(TrafficStatus.unknown, Analyzer.getStatusFromPixel(image, 0, 0));
		Assert.assertEquals(TrafficStatus.normal, Analyzer.getStatusFromPixel(image, 105, 213));
		Assert.assertEquals(TrafficStatus.slow, Analyzer.getStatusFromPixel(image, 110, 192));
		Assert.assertEquals(TrafficStatus.slow, Analyzer.getStatusFromPixel(image, 146, 309));
		Assert.assertEquals(TrafficStatus.stopped, Analyzer.getStatusFromPixel(image, 121, 173));
	}

	private BufferedImage getImage() throws IOException {
		BufferedImage image = ImageIO.read(new File("src/test/resources/bison-color.png"));
		return image;
	}

	// @Test
	public void shouldDisplayHistoryAsString() throws Exception {
		// File historyFile = new File("src/test/resources/bison-03-23-17_00_00.json");
		File historyFile = new File("src/test/resources/bison-03-23-18_00_00_custom.json");
		logger.debug("history loaded");
		ObjectMapper mapper = new ObjectMapper();

		TrafficHistory history = mapper.readValue(historyFile, TrafficHistory.class);
		System.out.println(mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(history.getHistoryAsString()));
		System.out.println(history.getHistorySummaryAsString());
	}

	@Test
	public void shouldReturnCurrentState() throws Exception {
		Analyzer analyzer = new Analyzer();
		TrafficState currentState = analyzer.getCurrentState(RocadeDirection.outer);

		Assert.assertNotNull(currentState);
		Assert.assertEquals(31, currentState.getState().values().size());
		logger.debug("{}", analyzer.getCurrentStateAsJSON(RocadeDirection.outer));
	}

	// @Test
	public void testAnalyseImage() throws Exception {
		Analyzer analyzer = new Analyzer();
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

				TrafficState currentState = analyzer.getCurrentStateFromImage(image, RocadeDirection.outer);
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
