package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import org.codehaus.jackson.map.ObjectMapper;
import org.elitefactory.jamming.model.RocadeDirection;
import org.elitefactory.jamming.model.TrafficHistory;
import org.elitefactory.jamming.model.TrafficState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {

	private static final Logger logger = LoggerFactory.getLogger(Runner.class);

	private static final int BATCH_SIZE = 2;

	private static final String IMAGES_FOLDER = "target/images";

	private static Analyzer imageAnalyzer = new Analyzer();
	private static ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) {
		int numberOfIterations = 60 * 2;

		try {
			File historyFile = new File("src/main/resources/history.json");
			logger.debug("history loaded");

			TrafficHistory history = mapper.readValue(historyFile, TrafficHistory.class);
			logger.debug("{} states restored", history.getNumberOfSamples());

			for (int i = 1; i < numberOfIterations; i++) {
				TrafficState currentState = imageAnalyzer.getCurrentState(RocadeDirection.outer);
				logger.debug("Getting state {}", currentState);

				history.putState(currentState);
				if (i % BATCH_SIZE == 0) {
					logger.debug("writing {} samples to file", BATCH_SIZE);
					mapper.writeValue(historyFile, history);
					mapper.writeValue(WebConnector.getFTPOutputStream("bison.json"), history);
					WebConnector.closeFTPConnection();
				}
				Thread.sleep(1000 * 60);
			}

		} catch (IOException e) {
			logger.error("Exception occured!!11", e);
		} catch (InterruptedException e) {
			logger.error("Exception occured!!11", e);
		}
	}

	public static void fetchBisonImages() throws IOException, InterruptedException {
		BufferedImage image = null;

		for (int i = 0; i < 60 * 2; i++) {
			image = WebConnector.getCurrentBisonImage();
			File file = new File(String.format(IMAGES_FOLDER + "/bison-%1$tm-%1$td-%1$tk_%1$tM.png", new Date()));

			logger.debug("saving file {}", file.getAbsolutePath());
			ImageIO.write(image, "png", file);

			Thread.sleep(60 * 1000l);
		}
	}
}
