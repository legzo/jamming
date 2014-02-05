package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.elitefactory.jamming.db.ESConnector;
import org.elitefactory.jamming.model.RocadeDirection;
import org.elitefactory.jamming.model.RocadePoint;
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
	 * Getting traffic info every minute (at 0 seconds)
	 */
	private static final String GET_TRAFFIC_CRON_EXPRESSION = "0 0/1 * * * ?";
	private static final boolean enableCrons = true;

	private static ObjectMapper mapper = new ObjectMapper();

	private static Logger logger = LoggerFactory.getLogger(Analyzer.class);

	private ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

	@Autowired
	private ESConnector connector;

	@PostConstruct
	public void init() {

		scheduler.initialize();

		Runnable getTrafficTask = new Runnable() {
			@Override
			public void run() {
				logger.info("Triggering getTrafficTask");
				TrafficState stateOuter = getCurrentState(RocadeDirection.outer);
				connector.save(stateOuter);
				TrafficState stateInner = getCurrentState(RocadeDirection.inner);
				connector.save(stateInner);

			}
		};
		if (enableCrons) {
			scheduler.schedule(getTrafficTask, new CronTrigger(
					GET_TRAFFIC_CRON_EXPRESSION));
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
	@Path("/config")
	@Produces("application/json")
	public String getCronsConfig() {
		try {
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("GET_TRAFFIC_CRON_EXPRESSION",
					GET_TRAFFIC_CRON_EXPRESSION);
			configMap.put("enableCrons", String.valueOf(enableCrons));

			ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
			return writer.writeValueAsString(configMap);
		} catch (IOException e) {
			logger.error("Exception occured while trying to get state from Bison", e);
		}
		return "Err";
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
		TrafficState trafficState = new TrafficState(new Date(), direction);
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

}
