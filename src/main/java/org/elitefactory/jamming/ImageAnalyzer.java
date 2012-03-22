package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.elitefactory.jamming.model.RocadePoint;
import org.elitefactory.jamming.model.TrafficHistory;
import org.elitefactory.jamming.model.TrafficState;
import org.elitefactory.jamming.model.TrafficStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

@Path("/traffic")
public class ImageAnalyzer {

	private static final String PING_CRON_EXPRESSION = "0 1/1 6,7,8,9,16,17,18,19 * * MON-FRI";
	private static final String UPLOAD_DATA_CRON_EXPRESSION = "0 0 7,8,9,16,17,18,19 * * MON-FRI";
	// private static final String PING_CRON_EXPRESSION = "0/1 * * * * MON-FRI";
	// private static final String UPLOAD_DATA_CRON_EXPRESSION = "30/30 * * * * MON-FRI";
	private static final String APPLICATION_URL = "http://freezing-winter-8090.herokuapp.com/rest/traffic";
	private static Logger logger = LoggerFactory.getLogger(ImageAnalyzer.class);
	private static ObjectMapper mapper = new ObjectMapper();
	private TrafficHistory history = new TrafficHistory();

	private ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

	@PostConstruct
	public void init() {

		scheduler.initialize();

		Runnable getTrafficTask = new Runnable() {
			@Override
			public void run() {
				logger.info("Triggering getTrafficTask");
				WebConnector.pingUrl(APPLICATION_URL);
				history.putState(getCurrentState());
			}
		};

		scheduler.schedule(getTrafficTask, new CronTrigger(PING_CRON_EXPRESSION));

		Runnable uploadTask = new Runnable() {
			@Override
			public void run() {
				logger.info("Triggering uploadTask");
				uploadHistory();
			}
		};

		scheduler.schedule(uploadTask, new CronTrigger(UPLOAD_DATA_CRON_EXPRESSION));
	}

	private void uploadHistory() {
		try {
			OutputStream ftpOutputStream = WebConnector.getFTPOutputStream(String.format(
					"bison-%1$tm-%1$td-%1$tk_%1$tM_%1$tS.json", new Date()));
			mapper.writeValue(ftpOutputStream, history);
			ftpOutputStream.close();
			WebConnector.closeFTPConnection();
			history.getStates().clear();
		} catch (IOException e) {
			logger.error("Exception occured while uploading data", e);
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

	public TrafficState getCurrentStateFromImage(BufferedImage image) {
		TrafficState trafficState = new TrafficState(new Date());
		for (RocadePoint rocadePoint : RocadePoint.values()) {
			TrafficStatus statusFromPixel = ImageAnalyzer.getStatusFromPixel(image, rocadePoint.x, rocadePoint.y);
			logger.trace("{}:{}", rocadePoint.name(), statusFromPixel.name());

			trafficState.setStatusForPoint(rocadePoint, statusFromPixel);
		}
		return trafficState;
	}

	public TrafficState getCurrentState() {
		return getCurrentStateFromImage(WebConnector.getCurrentBisonImage());
	}

	@GET
	@Produces("application/json")
	public String getCurrentStateAsJSON() {
		try {
			ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
			return writer.writeValueAsString(getCurrentState());
		} catch (IOException e) {
			logger.error("Exception occured while trying to get state from Bison", e);
		}
		return "Err";
	}

}
