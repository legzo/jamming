package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import junit.framework.Assert;

import org.elitefactory.jamming.model.TrafficStatus;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class ImageAnalyzerTest {


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

}
