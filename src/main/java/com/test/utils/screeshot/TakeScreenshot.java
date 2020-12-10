package com.test.utils.screeshot;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface TakeScreenshot {

	/**
	 * is isDryRun = true ---> classFolderName=className else className_nanotime
	 * 
	 */

	public void createClassFolder();

	/**
	 * creates folder with given method name
	 * 
	 * @param name
	 */
	public void createMethodFolder(String name);

	/**
	 * takes screenshot with driver instance and stores in file with name
	 * isdryrun=true -> methodname_index else method_index_nanotime stores in
	 * methodFolder
	 * 
	 * @param driver
	 * @param index
	 */
	public void takeScreenShot(WebDriver driver, int index);

	public void takeScreenShot(WebDriver driver, int index, String appendName);

	public void takeScreenShot(WebDriver driver, WebElement elem, int index);

	public void takeScreenShot(WebDriver driver, WebElement elem, int index, String appendName);

	void captureScreenShot(WebDriver driver, String fileName);

	public void scrollElementToUserView(WebDriver driver, WebElement elem);

	void captureScreenShot(WebDriver driver, WebElement element, String fileName);

	public boolean getIsDryRun();

	public String getBaseDirLocation();

	public String getCurrentDirLocation();

	public boolean getCompareImages();

}
