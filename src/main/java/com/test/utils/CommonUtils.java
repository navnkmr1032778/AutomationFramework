package com.test.utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.test.constants.WebDriverConstants;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class CommonUtils {

	Boolean driverFilefound = false;
	Session session = null;
	Channel channel = null;
	ChannelSftp channelSftp = null;

	protected static Logger logger = LoggerFactory.getLogger(CommonUtils.class.getName());

	public String getCurrentWorkingDirectory() {
		String workingDir = null;
		try {
			workingDir = System.getProperty("user.dir");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return workingDir;
	}

	public String getTestDataFullDirPath(String fileName) {
		String path = WebDriverConstants.PATH_TO_TEST_DATA_FILE;
		if (OSCheck.getOperatingSystemType() == OSCheck.OSType.Windows)
			path = WebDriverConstants.WINDOWS_PATH_TO_TEST_DATA_DIR;
		return (getCurrentWorkingDirectory() + path + fileName);
	}

	public void captureFullBrowserScreenShot(String imageName, WebDriver webDriver) {
		try {
			Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(webDriver);
			File dir = new File(WebDriverConstants.PATH_TO_BROWSER_SCREENSHOT);
			if (!dir.exists()) {
				try {
					dir.mkdir();
					logger.info("creating directory: " + dir);
				} catch (Exception ex) {
					logger.info("Couldn't create Directory" + ExceptionUtils.getRootCauseStackTrace(ex));
				}
			}
			ImageIO.write(screenshot.getImage(), "PNG", new File(
					WebDriverConstants.PATH_TO_BROWSER_SCREENSHOT + imageName + System.currentTimeMillis() + ".png"));
		} catch (Exception ex) {
			logger.info("Couldn't take Screenshot" + ExceptionUtils.getRootCauseStackTrace(ex));
		}
	}

	public void captureBrowserScreenShot(String imageName, WebDriver webDriver) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		String curDate = dateFormat.format(date);
		File dir = new File(WebDriverConstants.PATH_TO_BROWSER_SCREENSHOT);
		if (!dir.exists()) {
			try {
				dir.mkdir();
				logger.info("creating directory: " + dir);
			} catch (Exception ex) {
				logger.info("Couldn't create Directory" + ExceptionUtils.getRootCauseStackTrace(ex));
			}
		}

		try {
			Set<String> handles = webDriver.getWindowHandles();
			String currentHandle = webDriver.getWindowHandle();
			int handleCount = 0;
			for (String handle : handles) {
				handleCount++;
				webDriver.switchTo().window(handle);
				webDriver.manage().window().maximize();
				try {
					Thread.sleep(500);
				} catch (Exception e) {
					logger.info(ExceptionUtils.getRootCauseStackTrace(e).toString());
				}
				screenShot(WebDriverConstants.PATH_TO_BROWSER_SCREENSHOT + imageName + "_handle" + handleCount + "_"
						+ curDate + "_" + System.currentTimeMillis() + ".png", webDriver);
			}
			webDriver.switchTo().window(currentHandle);
		} catch (Exception ex) {
			logger.info("exception in taking Screenshot" + ExceptionUtils.getRootCauseStackTrace(ex));
		}
	}

	public void captureFullBrowserScreenShotForExtent(String imageName, WebDriver webDriver) {
		try {
			Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.simple()).takeScreenshot(webDriver);
			ImageIO.write(screenshot.getImage(), "PNG", new File(imageName));
		} catch (Exception ex) {
			logger.info("Couldn't take Screenshot" + ExceptionUtils.getStackTrace(ex));
		}
	}

	public void screenShot(String fileName, WebDriver webDriver) {
		try {
			File scrFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(scrFile, new File(fileName));
		} catch (IOException e) {
			logger.info("Error While taking Screen Shot");
			e.printStackTrace();
		}
	}

	public static String getCurrentTimeString() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//			sdf.setTimeZone(TimeZone.getDefault());
			sdf.setTimeZone(TimeZone.getDefault());
			Date date = new Date();
			return sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Date getDateFromString(String dateString, SimpleDateFormat formatter) {
		Date date = null;
		try {
			date = formatter.parse(dateString);

		} catch (Exception e) {
			logger.error(ExceptionUtils.getRootCauseStackTrace(e).toString());
		}
		return date;
	}

	public String getStringFromDate(Date d, SimpleDateFormat formatter) {
		return formatter.format(d);
	}

	public String getDateToday() {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		return getDateToday(df);

	}

	public String getDateToday(SimpleDateFormat formatter) {
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return formatter.format(new Date());
	}

	public boolean isFileExists(String fileName) {
		return new File(fileName).exists();
	}

}
