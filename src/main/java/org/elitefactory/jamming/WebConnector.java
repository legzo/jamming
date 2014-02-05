package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebConnector {

	private static final Logger logger = LoggerFactory.getLogger(WebConnector.class);

	private static final String BISON_URL = "http://diffusion-numerique.info-routiere.gouv.fr/asteccli/servlet/clientleger?format=png&source0=cigt_alienor&source1=cir&raster=bordeaux&ts=1391614540436";

	private static final boolean useProxy = false;

	public static BufferedImage getCurrentBisonImage() {
		try {
			HttpGet httpget = new HttpGet(BISON_URL);
			HttpResponse response = getHTTP().execute(httpget);

			BufferedImage image = ImageIO.read(response.getEntity().getContent());
			return image;
		} catch (IOException e) {
			logger.error("Error getting image {}", e);
		}
		return null;
	}

	public static HttpClient getHTTP() {

		if (useProxy) {
			HttpHost proxy = new HttpHost("localhost", 3128);
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(
					proxy);
			CloseableHttpClient httpClient = HttpClients.custom()
					.setRoutePlanner(routePlanner).build();

			return httpClient;
		} else {
			return HttpClientBuilder.create().build();
		}

	}

}
