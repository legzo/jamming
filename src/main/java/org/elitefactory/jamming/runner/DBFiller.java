package org.elitefactory.jamming.runner;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.elitefactory.jamming.WebConnector;
import org.elitefactory.jamming.db.State;
import org.elitefactory.jamming.db.StateDao;
import org.elitefactory.jamming.model.TrafficHistory;
import org.elitefactory.jamming.model.TrafficState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DBFiller {

	private static ObjectMapper mapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(Runner.class);

	public static void main(String[] args) {

		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml" });

		StateDao stateDao = context.getBean(StateDao.class);

		List<String> files = WebConnector.getFilesList();

		if (files != null && files.size() > 0) {
			for (String file : files) {
				String fileContent = WebConnector.getFile(file);
				logger.debug("Fetched file {}", file);

				try {
					TrafficHistory history = mapper.readValue(fileContent, TrafficHistory.class);

					Map<Date, TrafficState> states = history.getStates();
					logger.debug("Found {} states in file", states.values().size());

					if (states != null && states.size() > 0) {
						for (TrafficState state : states.values()) {
							stateDao.save(new State(state));
							logger.debug("Saved state to db {} - {}", state.getFormattedTime(), state.getStateAsFloat());
						}
					}
				} catch (IOException e) {
					logger.error("Could not read value", e);
				}
			}
		}
	}
}
