package org.elitefactory.jamming;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.elitefactory.jamming.model.TrafficHistory;
import org.elitefactory.jamming.model.TrafficState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {

	private static final Logger logger = LoggerFactory.getLogger(Runner.class);

	private static final int BATCH_SIZE = 2;

	private static ImageAnalyzer imageAnalyzer = new ImageAnalyzer();
	private static ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) {
		int numberOfIterations = 60 * 2;

		try {
			File historyFile = new File("src/main/resources/history.json");
			logger.debug("history loaded");

			TrafficHistory history = mapper.readValue(historyFile, TrafficHistory.class);
			logger.debug("{} states restored", history.getNumberOfSamples());

			for (int i = 1; i < numberOfIterations; i++) {
				TrafficState currentState = imageAnalyzer.getCurrentState();
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

}
