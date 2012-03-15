package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class ImageAnalyzerTest {

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
		System.out.println("R" + red + ", G" + green + ", B" + blue);
	}

}
