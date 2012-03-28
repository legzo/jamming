package org.elitefactory.jamming;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.cxf.helpers.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebConnector {

	private static final Logger logger = LoggerFactory.getLogger(WebConnector.class);

	private static final String BISON_URL = "http://www.bison-fute.equipement.gouv.fr/asteccli/servlet/clientleger?format=png&source0=cigt_alienor&source1=cir&raster=bordeaux";
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

	public static String getFile(String filename) {
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet("http://tetarot.free.fr/bison/" + filename);
			HttpResponse response = httpClient.execute(httpget);

			return IOUtils.readStringFromStream(response.getEntity().getContent());
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

	/**
	 * Not threadsafe
	 * 
	 * @return
	 * @throws IOException
	 */
	public static OutputStream getFTPOutputStream(String filename) throws IOException {
		connectToFTP();

		OutputStream outputStream = ftpClient.storeFileStream(filename);
		logger.debug("store {}", ftpClient.getReplyString());
		if (outputStream == null) {
			logger.error("FTP outputStream is empty!!");
		}

		return outputStream;
	}

	private static void connectToFTP() throws SocketException, IOException {
		ftpClient.connect("ftpperso.free.fr");
		ftpClient.login("tetarot", "kem6mool");
		logger.debug("connect {}", ftpClient.getReplyString());

		ftpClient.enterLocalPassiveMode();

		ftpClient.changeWorkingDirectory("bison");
	}

	public static void closeFTPConnection() throws IOException {
		ftpClient.completePendingCommand();
		ftpClient.disconnect();
	}

	public static List<String> getFilesList() {
		try {
			List<String> results = new ArrayList<String>();

			connectToFTP();
			FTPFile[] listOfFiles = ftpClient.listFiles();

			for (FTPFile ftpFile : listOfFiles) {
				if (ftpFile.isFile()) {
					results.add(ftpFile.getName());
				}
			}
			ftpClient.disconnect();
			return results;
		} catch (IOException e) {
			logger.error("Error getting image {}", e);
		}
		return null;
	}
}
