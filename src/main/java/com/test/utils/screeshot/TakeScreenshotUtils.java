package com.test.utils.screeshot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.test.constants.WebDriverConstants;
import com.test.webdriverhelpers.BaseDriverHelper;

public class TakeScreenshotUtils implements TakeScreenshot {

	String fileFolderName;
	String baseDirectoryLocation, currentDirectoryLocation;
	boolean isDryRun, isClassFolderCreated = false, isCompareImages;
	List<String> methodFolderNames;

	public TakeScreenshotUtils(boolean isDryRun, String baseDirectoryLocation, String currentDirectoryLocation,
			boolean isCompareImages) {
		this.isDryRun = isDryRun;
		this.baseDirectoryLocation = baseDirectoryLocation;
		this.currentDirectoryLocation = currentDirectoryLocation;
		this.isCompareImages = isCompareImages;
		methodFolderNames = new ArrayList<String>();
	}

	/**
	 * creates class folder and device_browser folder
	 */

	public void createClassFolder() {
		synchronized (this) {
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
			String folderName = stackTraceElements[3].getFileName().replace(".java", "");
			if (!isDryRun) {
				folderName = folderName + "_" + System.nanoTime();
			}
			this.fileFolderName = WebDriverConstants.PATH_TO_BROWSER_SCREENSHOT_BASE;
			if (isDryRun) {
				fileFolderName = getBaseDirLocation();
			} else if (getCompareImages()) {
				fileFolderName = getCurrentDirLocation();
			}
			fileFolderName = fileFolderName + "/" + folderName;
			File dir = new File(fileFolderName);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		createDeviceBrowserFolder();
		isClassFolderCreated = true;
	}

	/**
	 * appends devicename_browsername to this.folderName
	 * (this.fileFolderName+="/devicename_browsername") -Dmobilename="device_name"
	 * -DemulDeviceName="noEmul"
	 */
	public void createDeviceBrowserFolder() {
		String deviceFolderName = getDeviceBrowserName();
		fileFolderName += "/" + deviceFolderName;
		File dir = new File(fileFolderName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	/**
	 * get device name and browser name
	 * 
	 * @return (devicename_browsername)
	 */
	public String getDeviceBrowserName() {
		String deviceFolderName = null;
		BaseDriverHelper helper = new BaseDriverHelper();
		deviceFolderName += "_" + helper.getBrowserToRun();
		return deviceFolderName;
	}

	public void createMethodFolder(String name) {
		synchronized (this) {
			File methodFolder = new File(name);
			if (methodFolder.exists()) {
				methodFolder.delete();
			}
			methodFolder.mkdirs();
			methodFolderNames.add(name);
		}
	}

	/***
	 * 
	 * @param index -> interger (index of screenshot within a method) screenshot
	 *              will be saved as methodName_index inside folder with name
	 *              methodName
	 */

	public void takeScreenShot(WebDriver driver, int index) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		String methodName = stackTraceElements[2].getMethodName();
		if (!isClassFolderCreated) {
			createClassFolder();
		}
		String methodFolderName = fileFolderName + "/" + methodName;
		String file;
		synchronized (this) {
			file = methodFolderName + "/" + methodName + "_" + index;
			if (!isDryRun) {
				file = file + "_" + System.nanoTime();
			}
			if (!methodFolderNames.contains(methodFolderName))
				createMethodFolder(methodFolderName);
		}
		file = file + ".png";
		captureScreenShot(driver, file);
	}

	/***
	 * 
	 * @param index      -> interger (index of screenshot within a method)
	 * @param appendName -> based on data from data provider
	 * 
	 *                   screenshot will base saved as methodName_index inside fold
	 *                   methodName_name
	 */

	public void takeScreenShot(WebDriver driver, int index, String appendName) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		String methodName = stackTraceElements[2].getMethodName() + "_" + appendName;
		if (!isClassFolderCreated) {
			createClassFolder();
		}
		String methodFolderName = fileFolderName + "/" + methodName;
		synchronized (this) {
			if (!methodFolderNames.contains(methodFolderName))
				createMethodFolder(methodFolderName);
		}

		String file = methodFolderName + "/" + methodName + "_" + index;
		file = file + ".png";
		captureScreenShot(driver, file);
	}

	public void takeScreenShot(WebDriver driver, WebElement elem, int index) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		String methodName = stackTraceElements[2].getMethodName();
		if (!isClassFolderCreated) {
			createClassFolder();
		}
		String methodFolderName = fileFolderName + "/" + methodName;
		String file;
		synchronized (this) {
			file = methodFolderName + "/" + methodName + "_" + index;
			if (!isDryRun) {
				file = file + "_" + System.nanoTime();
			}
			if (!methodFolderNames.contains(methodFolderName))
				createMethodFolder(methodFolderName);
		}
		file = file + ".png";
		captureScreenShot(driver, elem, file);
	}

	public void takeScreenShot(WebDriver driver, WebElement elem, int index, String appendName) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		String methodName = stackTraceElements[2].getMethodName() + "_" + appendName;
		if (!isClassFolderCreated) {
			createClassFolder();
		}
		String methodFolderName = fileFolderName + "/" + methodName;
		synchronized (this) {
			if (!methodFolderNames.contains(methodFolderName))
				createMethodFolder(methodFolderName);
		}

		String file = methodFolderName + "/" + methodName + "_" + index;
		file = file + ".png";
		captureScreenShot(driver, elem, file);
	}

	public void captureScreenShot(WebDriver driver, String fileName) {
		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void scrollElementToUserView(WebDriver driver, WebElement elem) {
		JavascriptExecutor javaScriptExecutor = (JavascriptExecutor) driver;
		javaScriptExecutor.executeScript(
				"window.scrollTo(" + (elem.getLocation().x - 500) + "," + (elem.getLocation().y - 500) + ");");
	}

	public void captureScreenShot(WebDriver driver, WebElement element, String fileName) {
		try {
			scrollElementToUserView(driver, element);
			File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			BufferedImage fullImg = ImageIO.read(scrFile);

			Point point = element.getLocation();
			int eleWidth = element.getSize().getWidth();
			int eleHeight = element.getSize().getHeight();

			if (fullImg.getHeight() > point.getY() && fullImg.getWidth() > point.getX()) {
				BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);
				ImageIO.write(eleScreenshot, "png", scrFile);
				FileUtils.copyFile(scrFile, new File(fileName));
			} else {
				System.out.println("image is size is less than element size");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean getIsDryRun() {
		return isDryRun;
	}

	public String getBaseDirLocation() {
		return baseDirectoryLocation;
	}

	public String getCurrentDirLocation() {
		return currentDirectoryLocation;
	}

	public boolean getCompareImages() {
		return isCompareImages;
	}
}
