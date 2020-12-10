package com.test.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.log4j.Logger;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@SuppressWarnings("deprecation")
public class FileDownloader {

	private static final Logger LOG = Logger.getLogger(FileDownloader.class);
	private WebDriver driver;
	private String localDownloadPath = System.getProperty("java.io.tmpdir");
	private boolean followRedirects = true;
	private boolean mimicWebDriverCookieState = true;
	private int httpStatusOfLastDownloadAttempt = 0;

	public FileDownloader() {

	}

	public FileDownloader(WebDriver driverObject) {
		this.driver = driverObject;
	}

	/**
	 * Specify if the FileDownloader class should follow redirects when trying to
	 * download a file
	 *
	 * @param value
	 */
	public void followRedirectsWhenDownloading(boolean value) {
		this.followRedirects = value;
	}

	/**
	 * Get the current location that files will be downloaded to.
	 *
	 * @return The filepath that the file will be downloaded to.
	 */
	public String localDownloadPath() {
		return this.localDownloadPath;
	}

	/**
	 * Set the path that files will be downloaded to.
	 *
	 * @param filePath The filepath that the file will be downloaded to.
	 */
	public void localDownloadPath(String filePath) {
		LOG.info("FILE PATH: " + filePath);
		this.localDownloadPath = filePath;
	}

	/**
	 * Download the file specified in the href attribute of a WebElement
	 *
	 * @param element
	 * @return
	 * @throws Exception
	 */
	public String downloadUsingHref(WebElement element) throws Exception {
		return downloader(element, "href");
	}

	/**
	 * Download the image specified in the src attribute of a WebElement
	 *
	 * @param element
	 * @return
	 * @throws Exception
	 */
	public String downloadUsingSrc(WebElement element) throws Exception {
		return downloader(element, "src");
	}

	/**
	 * Gets the HTTP status code of the last download file attempt
	 *
	 * @return
	 */
	public int getHTTPStatusOfLastDownloadAttempt() {
		return this.httpStatusOfLastDownloadAttempt;
	}

	/**
	 * Mimic the cookie state of WebDriver (Defaults to true) This will enable you
	 * to access files that are only available when logged in. If set to false the
	 * connection will be made as an anonymous user
	 *
	 * @param value
	 */
	public void mimicWebDriverCookieState(boolean value) {
		this.mimicWebDriverCookieState = value;
	}

	/**
	 * Load in all the cookies WebDriver currently knows about so that we can mimic
	 * the browser cookie state
	 *
	 * @param seleniumCookieSet
	 * @return
	 */
	private BasicCookieStore mimicCookieState(Set<Cookie> seleniumCookieSet) {
		BasicCookieStore mimicWebDriverCookieStore = new BasicCookieStore();
		for (Cookie seleniumCookie : seleniumCookieSet) {
			BasicClientCookie duplicateCookie = new BasicClientCookie(seleniumCookie.getName(),
					seleniumCookie.getValue());
			duplicateCookie.setDomain(seleniumCookie.getDomain());
			duplicateCookie.setSecure(seleniumCookie.isSecure());
			duplicateCookie.setExpiryDate(seleniumCookie.getExpiry());
			duplicateCookie.setPath(seleniumCookie.getPath());
			mimicWebDriverCookieStore.addCookie(duplicateCookie);
		}

		return mimicWebDriverCookieStore;
	}

	public String getDownloadableFileNameUsingHref(WebElement element) throws Exception {
		return getDownloadableFileName(element, "href");
	}

	private String getDownloadableFileName(WebElement element, String attribute) throws Exception {
		String fileToDownloadLocation = element.getAttribute(attribute);
		if (fileToDownloadLocation.trim().equals(""))
			throw new NullPointerException("The element you have specified does not link to anything!");
		URL fileToDownload = new URL(fileToDownloadLocation);
		return fileToDownload.getFile();
	}

	/**
	 * Perform the file/image download.
	 *
	 * @param element
	 * @param attribute
	 * @return
	 * @throws IOException
	 * @throws NullPointerException
	 */
	@SuppressWarnings("resource")
	private String downloader(WebElement element, String attribute)
			throws IOException, NullPointerException, URISyntaxException {

		String fileToDownloadLocation = element.getAttribute(attribute);
		if (fileToDownloadLocation.trim().equals(""))
			throw new NullPointerException("The element you have specified does not link to anything!");

		URL fileToDownload = new URL(fileToDownloadLocation);
		File downloadedFile = new File(this.localDownloadPath);
		if (downloadedFile.canWrite() == false)
			downloadedFile.setWritable(true);

		HttpClient client = new DefaultHttpClient();
		BasicHttpContext localContext = new BasicHttpContext();

		LOG.info("Mimic WebDriver cookie state: " + this.mimicWebDriverCookieState);
		if (this.mimicWebDriverCookieState) {
			localContext.setAttribute(ClientContext.COOKIE_STORE, mimicCookieState(this.driver.manage().getCookies()));
		}

		HttpGet httpget = new HttpGet(fileToDownload.toURI());
		HttpParams httpRequestParameters = httpget.getParams();
		httpRequestParameters.setParameter(ClientPNames.HANDLE_REDIRECTS, this.followRedirects);
		httpget.setParams(httpRequestParameters);

		LOG.info("Sending GET request for: " + httpget.getURI());
		HttpResponse response = client.execute(httpget, localContext);
		this.httpStatusOfLastDownloadAttempt = response.getStatusLine().getStatusCode();
		LOG.info("HTTP GET request status: " + this.httpStatusOfLastDownloadAttempt);
		LOG.info("Downloading file: " + downloadedFile.getName());
		InputStream inputStream = null;
		OutputStream outputStream = null;

		try {
			// read this file into InputStream
			inputStream = response.getEntity().getContent();

			// write the inputStream to a FileOutputStream
			outputStream = new FileOutputStream(downloadedFile);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					// outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		response.getEntity().getContent().close();
		String downloadedFileAbsolutePath = downloadedFile.getAbsolutePath();
		LOG.info("File downloaded to '" + downloadedFileAbsolutePath + "'");

		return downloadedFileAbsolutePath;
	}

	/**
	 * 
	 * @param fileName - download location
	 * @param fileUrl  - download url
	 * @throws MalformedURLException
	 * @throws IOException
	 */

	// Using Java IO
	public static void saveFileFromUrlWithJavaIO(String fileName, String fileUrl)
			throws MalformedURLException, IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(fileUrl).openStream());
			fout = new FileOutputStream(fileName);

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1)
				fout.write(data, 0, count);
		} finally {
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
		}
	}

	// Using Commons IO library
	// Available at http://commons.apache.org/io/download_io.cgi
	public static void saveFileFromUrlWithCommonsIO(String fileName, String fileUrl)
			throws MalformedURLException, IOException {
		FileUtils.copyURLToFile(new URL(fileUrl), new File(fileName));
	}

	/**
	 * @param zipFile      - absolute path of zip file
	 * @param outputFolder - absolute path of unzip folder to save
	 */

	public void unZipIt(String zipFile, String outputFolder) {
		byte[] buffer = new byte[1024];
		try {
			// create output directory is not exists
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);

				LOG.info("file unzip : " + newFile.getAbsoluteFile());

				if (ze.isDirectory()) {
					if (!newFile.exists())
						newFile.mkdirs();
					ze = zis.getNextEntry();
					continue;
				}

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
