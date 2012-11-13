package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.elitefactory.jamming.db.State;
import org.elitefactory.jamming.db.StateDao;
import org.elitefactory.jamming.db.StateLight;
import org.elitefactory.jamming.model.RocadeDirection;
import org.elitefactory.jamming.model.RocadePoint;
import org.elitefactory.jamming.model.TrafficHistory;
import org.elitefactory.jamming.model.TrafficState;
import org.elitefactory.jamming.model.TrafficStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Path("/")
@Component
public class Analyzer {

	/**
	 * Pinging heroku app every ten minutes, night and day, everyday
	 */
	private static final String PING_CRON_EXPRESSION = "0 0/10 * * * *";

	/**
	 * Getting traffic info every minute (at 0 seconds) from 07h00'00 to 09h59'00
	 */
	private static final String GET_INNER_TRAFFIC_CRON_EXPRESSION = "0 0/1 7,8,9 * * MON-FRI";
	/**
	 * Getting traffic info every minute (at 0 seconds) from 17h00'00 to 19h59'00
	 */
	private static final String GET_OUTER_TRAFFIC_CRON_EXPRESSION = "0 0/1 17,18,19 * * MON-FRI";

	/**
	 * Uploading @ 09h59'59" & 20h59'59" every week day
	 */
	private static final String UPLOAD_DATA_CRON_EXPRESSION = "59 59 9,19 * * MON-FRI";

	private static final boolean enableCrons = true;

	private static final String APPLICATION_URL = "http://freezing-winter-8090.herokuapp.com/rest/traffic/config";
	private static ObjectMapper mapper = new ObjectMapper();
	private TrafficHistory history = new TrafficHistory();

	private static Logger logger = LoggerFactory.getLogger(Analyzer.class);

	private ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

	@Autowired
	private StateDao stateDao;

	@PostConstruct
	public void init() {

		scheduler.initialize();

		Runnable getOuterTrafficTask = new Runnable() {
			@Override
			public void run() {
				logger.info("Triggering getOuterTrafficTask");
				// saveCurrentState(RocadeDirection.outer);
				history.putState(getCurrentState(RocadeDirection.outer));
			}
		};
		Runnable getInnerTrafficTask = new Runnable() {
			@Override
			public void run() {
				logger.info("Triggering getInnerTrafficTask");
				// saveCurrentState(RocadeDirection.inner);
				history.putState(getCurrentState(RocadeDirection.inner));
			}
		};

		Runnable pingTask = new Runnable() {
			@Override
			public void run() {
				logger.info("Triggering pingTask");
				WebConnector.pingUrl(APPLICATION_URL);
			}
		};

		Runnable uploadTask = new Runnable() {
			@Override
			public void run() {
				logger.info("Triggering uploadTask");
				uploadHistory();
			}
		};
		if (enableCrons) {
			scheduler.schedule(pingTask, new CronTrigger(PING_CRON_EXPRESSION));
			scheduler.schedule(getInnerTrafficTask, new CronTrigger(GET_INNER_TRAFFIC_CRON_EXPRESSION));
			scheduler.schedule(getOuterTrafficTask, new CronTrigger(GET_OUTER_TRAFFIC_CRON_EXPRESSION));
			scheduler.schedule(uploadTask, new CronTrigger(UPLOAD_DATA_CRON_EXPRESSION));
		}
	}

	@GET
	@Path("/current/{direction}")
	@Produces("application/json")
	public String getCurrentStateAsJSON(@PathParam("direction") RocadeDirection direction) {
		try {
			ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
			return writer.writeValueAsString(getCurrentState(direction));
		} catch (IOException e) {
			logger.error("Exception occured while trying to get state from Bison", e);
		}
		return "Err";
	}

	@GET
	@Path("/files/{filename}")
	@Produces("application/json")
	public String getJSONFile(@PathParam("filename") String filename) {
		return WebConnector.getFile(filename);
	}

	@GET
	@Path("/files")
	@Produces("application/json")
	public String getFilesList() {
		List<String> filesList = WebConnector.getFilesList();
		try {
			return mapper.writeValueAsString(filesList);
		} catch (IOException e) {
			logger.error("Exception occured while trying to get files list", e);
		}
		return "Err";
	}

	@GET
	@Path("/config")
	@Produces("application/json")
	public String getCronsConfig() {
		try {
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("PING_CRON_EXPRESSION", PING_CRON_EXPRESSION);
			configMap.put("GET_INNER_TRAFFIC_CRON_EXPRESSION", GET_INNER_TRAFFIC_CRON_EXPRESSION);
			configMap.put("GET_OUTER_TRAFFIC_CRON_EXPRESSION", GET_OUTER_TRAFFIC_CRON_EXPRESSION);
			configMap.put("UPLOAD_DATA_CRON_EXPRESSION", UPLOAD_DATA_CRON_EXPRESSION);

			ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
			return writer.writeValueAsString(configMap);
		} catch (IOException e) {
			logger.error("Exception occured while trying to get state from Bison", e);
		}
		return "Err";
	}

	@GET
	@Path("/history")
	@Produces("application/json")
	public String getHistory() {
		try {
			if (history != null && history.getStates().size() > 0) {
				ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
				return writer.writeValueAsString(history);
			} else {
				return "History is empty";
			}

		} catch (IOException e) {
			logger.error("Exception occured while trying to get state from Bison", e);
		}
		return "Err";
	}

	@GET
	@Path("/states/{from}/{to}")
	@Produces("application/json")
	public String getStates(@PathParam(value = "from") String from, @PathParam(value = "to") String to) {
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH_mm");

			Date fromDate = sdf.parse(from);
			Date toDate = sdf.parse(to);
			List<State> results = stateDao.find(fromDate, toDate);

			if (results != null && results.size() > 0) {
				TrafficHistory resultsAsHistory = new TrafficHistory();

				for (State state : results) {
					resultsAsHistory.putState(state.unmarshall());
				}

				ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
				return writer.writeValueAsString(resultsAsHistory);
			} else {
				return "History is empty";
			}

		} catch (IOException e) {
			logger.error("Exception occured while trying to get state from Bison", e);
		} catch (ParseException e) {
			logger.error("Parsing date parameter failed", e);
		}
		return "Err";
	}

	@GET
	@Path("/statesForDay/{dayIndex}/{ampm}")
	@Produces("application/json")
	public String getStatesForDay(@PathParam(value = "dayIndex") int dayIndex, @PathParam(value = "ampm") String ampm) {
		try {
			logger.debug("Getting states from DB for day {}:{}", dayIndex, ampm);
			List<State> results = stateDao.find(dayIndex);
			logger.debug("{} states loaded", results.size());
			if (results != null && results.size() > 0) {
				List<StateLight> realResults = new ArrayList<StateLight>();

				for (State state : results) {

					Calendar cal = Calendar.getInstance();
					cal.setTime(state.getTime());

					if ((ampm.equals("AM") && cal.get(Calendar.AM_PM) == Calendar.AM)
							|| (ampm.equals("PM") && cal.get(Calendar.AM_PM) == Calendar.PM)) {
						if (state.getSummary() > 0) {
							realResults.add(new StateLight(state));
						}
					}
				}
				logger.debug("filtering finished, {} states retained", realResults.size());
				ObjectWriter writer = mapper.writer();
				return writer.writeValueAsString(realResults);
			} else {
				return "History is empty";
			}

		} catch (IOException e) {
			logger.error("Exception occured while trying to get state from Bison", e);
		}
		return "Err";
	}

	private void uploadHistory() {
		uploadHistory(0);
	}

	private void uploadHistory(int retryNumber) {
		try {
			// if it is a retry attempt, wait for 4s before retrying
			if (retryNumber > 0) {
				Thread.sleep(4000);
			}

			OutputStream ftpOutputStream = WebConnector.getFTPOutputStream(String.format(
					"bison-%1$tm-%1$td-%1$tH_%1$tM_%1$tS.json", new Date()));
			mapper.writeValue(ftpOutputStream, history);
			ftpOutputStream.close();
			WebConnector.closeFTPConnection();
			history.getStates().clear();
		} catch (IOException e) {
			if (retryNumber < 4) {
				logger.warn("Upload has failed, retrying for {} time", retryNumber);
				uploadHistory(retryNumber + 1);
			} else {
				logger.error("Exception occured while uploading data, all retry attempts have failed", e);
			}
		} catch (InterruptedException e) {
			logger.error("Thread wait interrupted", e);
		}
	}

	protected static TrafficStatus getStatusFromPixel(BufferedImage image, int x, int y) {
		int pixel = image.getRGB(x, y);

		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;

		switch (green) {
		case 255:
			return TrafficStatus.normal;
		case 153:
			return TrafficStatus.slow;
		case 0:
			if (red == 255) {
				return TrafficStatus.stopped;
			}
		default:
			return TrafficStatus.unknown;
		}
	}

	public TrafficState getCurrentStateFromImage(BufferedImage image, RocadeDirection direction) {
		TrafficState trafficState = new TrafficState(new Date());
		for (RocadePoint rocadePoint : RocadePoint.getPointsForDirection(direction)) {
			TrafficStatus statusFromPixel = Analyzer.getStatusFromPixel(image, rocadePoint.x, rocadePoint.y);
			logger.trace("{}:{}", rocadePoint.name(), statusFromPixel.name());

			trafficState.setStatusForPoint(rocadePoint, statusFromPixel);
		}
		return trafficState;
	}

	public TrafficState getCurrentState(RocadeDirection direction) {
		return getCurrentStateFromImage(WebConnector.getCurrentBisonImage(), direction);
	}

	public void setStateDao(StateDao stateDao) {
		this.stateDao = stateDao;
	}
}
