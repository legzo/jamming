package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.elitefactory.jamming.model.RocadePoint;
import org.elitefactory.jamming.model.TrafficState;
import org.elitefactory.jamming.model.TrafficStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/traffic")
public class ImageAnalyzer {

	private static Logger logger = LoggerFactory.getLogger(ImageAnalyzer.class);
	private static ObjectMapper mapper = new ObjectMapper();

	public static TrafficStatus getStatusFromPixel(BufferedImage image, int x, int y) {
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

	public TrafficState getCurrentStateFromImage(BufferedImage image) throws IOException {
		TrafficState trafficState = new TrafficState(new Date());
		for (RocadePoint rocadePoint : RocadePoint.values()) {
			TrafficStatus statusFromPixel = ImageAnalyzer.getStatusFromPixel(image, rocadePoint.x, rocadePoint.y);
			logger.trace("{}:{}", rocadePoint.name(), statusFromPixel.name());

			trafficState.setStatusForPoint(rocadePoint, statusFromPixel);
		}
		return trafficState;
	}

	public TrafficState getCurrentState() throws IOException {
		return getCurrentStateFromImage(ImageGetter.getCurrentBisonImage());
	}

	@GET
	@Produces("application/json")
	public String getCurrentStateAsJSON() throws IOException {
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		return writer.writeValueAsString(getCurrentState());
	}

}
