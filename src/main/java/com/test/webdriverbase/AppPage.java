package com.test.webdriverbase;

import java.time.Duration;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestListenerAdapter;

import com.test.constants.WebDriverConstants;
import com.test.utils.screeshot.TakeScreenshot;
import com.test.utils.screeshot.TakeScreenshotUtils;
import com.test.webdriverhelpers.BaseDriverHelper;

public class AppPage extends TestListenerAdapter {
	protected static Logger logger = LoggerFactory.getLogger(AppPage.class.getName());
	protected WebDriver driver;

	JavascriptExecutor javaScriptExecutor;
	BaseDriverHelper baseDriverHelper = new BaseDriverHelper();

	enum ByTypes {
		INDEX, VALUE, TEXT
	}

	enum JavaScriptSelector {
		ID, CLASS, NAME, TAGNAME
	}

	public AppPage(WebDriver driver) {
		this.driver = driver;
		waitForPageLoadComplete();
		PageFactory.initElements(driver, this);
		// android does not supports maximizeWindow;
		maximizeWindow();
	}

	public void takeScreenShot(String fileName) {
		TakeScreenshot ts = new TakeScreenshotUtils(false, "", "", false);
		ts.captureScreenShot(driver, fileName);
	}

	public void takeScreenShot() {
		String fileName = WebDriverConstants.PATH_TO_BROWSER_SCREENSHOT_BASE;
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		fileName = fileName + stackTraceElements[2].getMethodName() + ".png";
		TakeScreenshot ts = new TakeScreenshotUtils(false, "", "", false);
		ts.captureScreenShot(driver, fileName);
	}

	public WebDriver getDriver() {
		return this.driver;
	}

	public void get(String url) {
		this.driver.get(url);
	}

	public String getCurrentUrl() {
		return this.driver.getCurrentUrl();
	}

	public String pageSource() {
		return this.driver.getPageSource();
	}

	public JavascriptExecutor getJavaScriptExecutor() {
		if (javaScriptExecutor == null)
			javaScriptExecutor = (JavascriptExecutor) driver;
		return javaScriptExecutor;
	}

	public boolean isElementPresent(By locator) {
		return this.driver.findElements(locator).size() == 0 ? false : true;
	}

	public boolean isElementPresent(WebElement element) {
		try {
			element.getAttribute("innerHTML");
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	public boolean isElementPresentAndDisplayed(WebElement element) {
		try {
			return isElementPresent(element) && element.isDisplayed();
		} catch (Exception ex) {
			return false;
		}
	}

	public boolean isElementPresentAndDisplayed(By xpath) {
		try {
			return isElementPresentAndDisplayed(this.driver.findElement(xpath));
		} catch (Exception ex) {
			return false;
		}

	}

	public void waitForPageLoadComplete() {
		waitForPageLoad(WebDriverConstants.MAX_TIMEOUT_PAGE_LOAD);
		waitForAJaxCompletion();
		return;
	}

	public void clearAndType(WebElement element, String text) {
		waitForElementToAppear(element);
		element.clear();
		element.sendKeys(text);
	}

	public void click(WebElement element) {
		waitForElementToAppear(element);
		scrollElementToUserView(element);
		element.click();
		sleep(1000);
	}

	public void click(By by) {
		WebElement element = getDriver().findElement(by);
		waitForElementToAppear(element);
		element.click();
		sleep(1000);
	}

	public void clickAndWaitForPageLoadComplete(WebElement element) {
		waitForElementToAppear(element);
		scrollElementToUserView(element);
		element.click();
		sleep(1000);
		waitForPageLoadComplete();
	}

	public void waitForPageLoad(int timeout) {
		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(Duration.ofSeconds(timeout))
				.pollingEvery(Duration.ofSeconds(5)).ignoring(NoSuchElementException.class, WebDriverException.class);
		wait.until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				String result = (String) getJavaScriptExecutor().executeScript("return document.readyState");
				if (result == null)
					return false;
				else
					return result.equals("complete");
			}
		});
		return;
	}

	public void maximizeWindow() {
		try {
			driver.manage().window().maximize();
		} catch (Exception e) {
			logger.debug("Exception while maximizing the window...");
			logger.debug(e.getMessage());
		}
	}

	public String getVisibleTextOfElement(WebElement elem) {
		String visibleText = (String) getJavaScriptExecutor().executeScript(
				"var clone = $(arguments[0]).clone();" + "clone.appendTo('body').find(':hidden').remove();"
						+ "var text = clone.text();" + "clone.remove(); return text;",
				elem);
		visibleText = visibleText.replaceAll("\\s+", " ");
		return visibleText;
	}

	public void refresh() {
		this.driver.navigate().refresh();
		monkeyPatch();
	}

	public void closeWindow() {
		this.driver.close();
		sleep(500);
	}

	public WebElement waitForElementToAppear(By locator) {
		WebDriverWait wait = new WebDriverWait(this.driver, WebDriverConstants.WAIT_TWO_MIN);
		wait.until(ExpectedConditions.elementToBeClickable(locator));
		return driver.findElement(locator);
	}

	public void waitForElementToAppear(WebElement e) {
		WebDriverWait wait = new WebDriverWait(this.driver, WebDriverConstants.WAIT_TWO_MIN);
		wait.until(ExpectedConditions.elementToBeClickable(e));
	}

	public void waitForElementToDisappear(By locator) {
		WebDriverWait wait = new WebDriverWait(this.driver, WebDriverConstants.WAIT_TWO_MIN);
		wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
	}

	public void waitForElementToDisappear(WebElement e) {
		WebDriverWait wait = new WebDriverWait(this.driver, WebDriverConstants.WAIT_TWO_MIN);
		if (isElementPresent(e))
			wait.until(invisibilityOfElementLocated(e));
	}

	public void waitForElementToDisappear(WebElement e, int timeOut) {
		WebDriverWait wait = new WebDriverWait(this.driver, timeOut);
		if (isElementPresent(e))
			wait.until(invisibilityOfElementLocated(e));
	}

	public void waitForElementToDisappear(String xpath, int timeOut) {
		WebDriverWait wait = new WebDriverWait(this.driver, timeOut);
		WebElement e = this.driver.findElement(By.xpath(xpath));
		if (isElementPresentAndDisplayed(e))
			wait.until(invisibilityOfElementLocated(e));
	}

	public ExpectedCondition<Boolean> invisibilityOfElementLocated(final WebElement element) {
		return new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				try {
					return !(element.isDisplayed());
				} catch (NoSuchElementException e) {
					// Returns true because the element is not present in DOM. The
					// try block checks if the element is present but is invisible.
					return true;
				} catch (StaleElementReferenceException e) {
					// Returns true because stale element reference implies that element
					// is no longer visible.
					return true;
				}
			}
		};
	}

	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public String getTitle() {
		return this.driver.getTitle();
	}

	public String getTextForElementIfPresent(By locator) {
		String text = null;
		if (isElementPresent(locator)) {
			text = this.driver.findElement(locator).getText();
		}
		return text;
	}

	public String getTextForElementIfPresent(WebElement e) {
		String text = null;
		if (isElementPresent(e)) {
			text = e.getText();
		}
		return text;
	}

	public Object executeScript(String script) {
		return ((JavascriptExecutor) this.driver).executeScript(script);
	}

	/**
	 * Will induce a wait till all ajax requests are completed. This works based on
	 * the monkey patch. If the monkey patch is not in place, it will apply the
	 * monkey patch first and returns without any wait.
	 */
	public void waitForAJaxCompletion() {
		try {
			ExpectedCondition<Boolean> isLoadingFalse = new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver driver) {
					String ajaxCount = (String) ((JavascriptExecutor) driver)
							.executeScript("return '' + XMLHttpRequest.prototype.ajaxCount");
					if (ajaxCount != null && ajaxCount.equals("undefined")) {
						monkeyPatch();
						return true;
					}
					if (ajaxCount != null && Double.parseDouble(ajaxCount) > 0.0d) {
						return false;
					} else {
						return true;
					}
				}
			};
			Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(Duration.ofMinutes(1))
					.pollingEvery(Duration.ofSeconds(5)).ignoring(NoSuchElementException.class);
			wait.until(isLoadingFalse);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getRootCauseStackTrace(e).toString());
		}
	}

	public void monkeyPatch() {

		String ajaxCount = (String) ((JavascriptExecutor) driver)
				.executeScript("return '' + XMLHttpRequest.prototype.ajaxCount");
		if (ajaxCount != null && ajaxCount.equals("undefined")) {
			getJavaScriptExecutor().executeScript(
					"!function(t){function n(){t.ajaxCount++,console.log(\"Ajax count when triggering ajax send: \"+t.ajaxCount)}function a(){t.ajaxCount>0&&t.ajaxCount--,console.log(\"Ajax count when resolving ajax send: \"+t.ajaxCount)}t.ajaxCount=0;var e=t.send;t.send=function(t){return this.addEventListener(\"readystatechange\",function(){null!=this&&this.readyState==XMLHttpRequest.DONE&&a()},!1),n(),e.apply(this,arguments)};var o=t.abort;return t.abort=function(t){return a(),o.apply(this,arguments)},t}(XMLHttpRequest.prototype);");
		}
	}

	public void goBack() {
		this.driver.navigate().back();
	}

	public void gotoURL(String url) {
		this.driver.get(url);
		waitForAJaxCompletion();
	}

	public void scrollElementToUserView(WebElement elem) {
		getJavaScriptExecutor().executeScript(
				"window.scrollTo(" + (elem.getLocation().x - 500) + "," + (elem.getLocation().y - 500) + ");");
	}

}