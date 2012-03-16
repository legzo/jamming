package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageGetter {

	private static final String IMAGES_FOLDER = "target/images";
	private static final String BISON_URL = "http://www.bison-fute.equipement.gouv.fr/asteccli/servlet/clientleger?format=png&source0=cigt_alienor&source1=cir&raster=bordeaux";
	private static final Logger logger = LoggerFactory.getLogger(ImageGetter.class);

	public static BufferedImage getCurrentBisonImage() throws IOException {

		HttpClient client = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(BISON_URL);
		HttpResponse response = client.execute(httpget);

		BufferedImage image = ImageIO.read(response.getEntity().getContent());
		return image;
	}

	public static void fetchBisonImages() throws IOException, InterruptedException {
		BufferedImage image = null;

		for (int i = 0; i < 60 * 2; i++) {
			image = getCurrentBisonImage();
			File file = new File(String.format(IMAGES_FOLDER + "/bison-%1$tm-%1$td-%1$tk_%1$tM.png", new Date()));

			logger.debug("saving file {}", file.getAbsolutePath());
			ImageIO.write(image, "png", file);

			Thread.sleep(60 * 1000l);
		}
	}
}
