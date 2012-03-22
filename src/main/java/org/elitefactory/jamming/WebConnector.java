package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebConnector {

	private static final String IMAGES_FOLDER = "target/images";
	private static final String BISON_URL = "http://www.bison-fute.equipement.gouv.fr/asteccli/servlet/clientleger?format=png&source0=cigt_alienor&source1=cir&raster=bordeaux";
	private static final Logger logger = LoggerFactory.getLogger(WebConnector.class);
	private static final FTPClient ftpClient = new FTPClient();

	public static BufferedImage getCurrentBisonImage() {
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(BISON_URL);
			HttpResponse response = httpClient.execute(httpget);

			BufferedImage image = ImageIO.read(response.getEntity().getContent());
			return image;
		} catch (IOException e) {
			logger.error("Error getting image {}", e);
		}
		return null;
	}

	public static void pingUrl(String urlToPing) {
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(urlToPing);
			HttpResponse result = httpClient.execute(httpget);
			logger.debug("Pinging {}, result {}", urlToPing, result.getStatusLine().getStatusCode());
		} catch (IOException e) {
			logger.error("Error pinging {}", e);
		}
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

	/**
	 * Not threadsafe
	 * 
	 * @return
	 */
	public static OutputStream getFTPOutputStream(String filename) {
		try {
			ftpClient.connect("ftpperso.free.fr");
			ftpClient.login("tetarot", "kem6mool");
			logger.debug("XXX connect {}", ftpClient.getReplyString());

			ftpClient.enterLocalPassiveMode();

			OutputStream outputStream = ftpClient.storeFileStream(filename);
			logger.debug("XXX store {}", ftpClient.getReplyString());
			if (outputStream == null) {
				logger.error("FTP outputStream is empty!!");
			}

			return outputStream;
		} catch (IOException e) {
			logger.error("Exception occured while getting FTP outputStream", e);
		}
		return null;
	}

	public static void closeFTPConnection() {
		try {
			ftpClient.completePendingCommand();
			ftpClient.disconnect();
		} catch (IOException e) {
			logger.error("Exception occured trying to close ftp connection", e);
		}
	}
}
