package com.test.webdriverhelpers;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import com.test.constants.WebDriverConstants;
import com.test.customexception.MyCoreExceptions;
import com.test.webdriverfactory.AppDriver;

import net.jsourcerer.webdriver.jserrorcollector.JavaScriptError;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;

public class BaseDriverHelper {

	WebDriver driver = null;
	WebDriver secondaryDriver = null;
	BrowserMobProxy proxyServer = null;

	Logger logger = getLogger(this.getClass());

	public void startServer() throws InterruptedException {

		logger.info("starting server");
		if (proxyServer != null)
			return;
		// port number equals to zero starts the server in dynamic port
		proxyServer = new BrowserMobProxyServer();
		try {
			proxyServer.start(0);
			/*
			 * proxyServer.
			 * 
			 * Start the server in specified host and port - TODO Map<String, String>
			 * options = new HashMap<String, String>(); options.put("httpProxy", "127.0.0.1"
			 * + ":" + "3000"); proxyServer.setOptions(options);
			 */

		} catch (Exception e) {
			String error = "Error while starting server.. " + e.getStackTrace();
			logger.error(error);
		}

	}

	@SuppressWarnings("unused")
	public void startDriver() throws Exception {
		logger.info("start driver");
		if (driver != null)
			return;
		String browserName = getBrowserToRun();

		Capabilities cap = null;
		cap = createDriverCapabilities(browserName);

		if (cap == null)
			throw new MyCoreExceptions("Capabilities return as Null");
		else {
			driver = setWebDriver(cap);
		}
	}

	public WebDriver setWebDriver(Capabilities cap) throws Exception {
		createProxy(cap);
		driver = startBrowser(cap);
		return driver;
	}

	private void setAdditionalCapabilities(Capabilities cap, String capabilityType, Object capabilityValue) {

		switch (cap.getClass().getSimpleName()) {
		case "ChromeOptions":
			ChromeOptions chromeOptions = (ChromeOptions) cap;
			chromeOptions.setCapability(capabilityType, capabilityValue);
			break;
		case "FirefoxOptions":
			FirefoxOptions firefoxOptions = (FirefoxOptions) cap;
			firefoxOptions.setCapability(capabilityType, capabilityValue);
			break;
		case "InternetExplorerOptions":
			InternetExplorerOptions internetExplorerOptions = (InternetExplorerOptions) cap;
			internetExplorerOptions.setCapability(capabilityType, capabilityValue);
			break;
		}

	}

	private String getBrowserName(String driverTypeStr) throws MyCoreExceptions {
		String browserName = WebDriverConstants.DEFAULT_BROWSER_NAME;
		try {
			switch (WebDriverConstants.DriverTypes.valueOf(driverTypeStr.toUpperCase())) {
			case PRIMARY:
				browserName = System.getProperty("webdriver.browser", WebDriverConstants.DEFAULT_BROWSER_NAME); // Setting
																												// the
																												// default
																												// browser
																												// if no
																												// browser
																												// name
																												// is
																												// specified
				break;
			case SECONDARY:
				browserName = System.getProperty("webdriver.secondary.browser",
						WebDriverConstants.DEFAULT_BROWSER_NAME);
				break;
			default:
				browserName = WebDriverConstants.DEFAULT_BROWSER_NAME;
			}
			browserName = browserName.toLowerCase();
			browserName = WebDriverConstants.DRIVER_METHOD.containsKey(browserName) ? browserName
					: WebDriverConstants.DEFAULT_BROWSER_NAME;
		} catch (Exception e) {
			throw new MyCoreExceptions("Exception while assiging browserName");
		}
		return browserName;
	}

	private Capabilities createDriverCapabilities(String browserName) {
		Capabilities cap = null;
		try {
			SetBrowserCapabilities setBrowserCapabilities = new SetBrowserCapabilities();
			Method setCapabilities = setBrowserCapabilities.getClass()
					.getMethod(WebDriverConstants.DRIVER_METHOD.get(browserName), null);
			cap = (Capabilities) setCapabilities.invoke(setBrowserCapabilities);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cap;
	}

	@SuppressWarnings("deprecation")
	private WebDriver startBrowser(Capabilities cap) {
		WebDriver driver = null;
		try {
			if (Boolean.parseBoolean(System.getProperty("userdocker", "false"))) {
				URL u = new URL("http://localhost:4444/wd/hub");
				driver = new RemoteWebDriver(u, cap);
				logger.info("docker driver started");
			} else {
				switch (WebDriverConstants.BrowserNames.valueOf(cap.getBrowserName().replace(" ", "_").toUpperCase())) {
				case CHROME:
					driver = new ChromeDriver(cap);
					break;
				case INTERNET_EXPLORER:
					driver = new InternetExplorerDriver(cap);
					break;
				case FIREFOX:
					driver = new FirefoxDriver(cap);
					break;
				case PHANTOMJS:
					driver = new PhantomJSDriver(cap);
					break;
				default:
					throw new IllegalArgumentException("Invalid Argument for browser name : " + cap.getBrowserName());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return driver;
	}

	private Proxy createProxyObject() {
		Proxy proxy = null;
		try {
			logger.info("-------------------------------proxy server - " + proxyServer.getPort());
			proxy = ClientUtil.createSeleniumProxy(proxyServer);// proxyServer.seleniumProxy();
			/*
			 * proxy.setSslProxy("trustAllSSLCertificates");
			 * proxy.setHttpProxy("localhost:"+proxyServer.getPort()); set server
			 * properties. proxyServer.setCaptureHeaders(true); capture headers
			 * proxyServer.setCaptureContent(true); capture content.
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
		return proxy;
	}

	private void createProxy(Capabilities cap) {
		try {
			Proxy proxy = createProxyObject();

			if (proxy != null) {
				setAdditionalCapabilities(cap, CapabilityType.PROXY, (Object) proxy);
			} else
				logger.info("Proxy object is null");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopServer() {
		try {
			if (proxyServer != null)
				proxyServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopDriver() {
		if (getDriver() != null) {
			getDriver().quit();
			setDriver(null);
		}
	}

	public void stopPrimaryDriver() {
		if (getDriver() != null) {
			getDriver().quit();
			setDriver(null);
		}
	}

	public WebDriver getDriver() {
		return this.driver;
	}

	public void setDriver(Object obj) {
		this.driver = (WebDriver) obj;
	}

	@SuppressWarnings("unused")
	private void printCapabilities(Capabilities capabilities) {
		Map<String, ?> map = capabilities.asMap();
		for (Entry<String, ?> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			logger.info("\t\tkey is " + key + "\t\tvalue is " + value);
		}
	}

	public Logger getLogger(Class<?> className) {
		Logger logger = null;
		try {
			logger = LoggerFactory.getLogger(className);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logger;
	}

	public String getPrimaryWinhandle() throws MyCoreExceptions {
		try {
			if (this.driver == null)
				throw new MyCoreExceptions("Unable to get the winhandle as the driver is set as null");
			return this.driver.getWindowHandle();
		} catch (Exception e) {
			e.printStackTrace();
			throw new MyCoreExceptions("Exception occured... " + e.getStackTrace());

		}
	}

	public WebDriver getDriverfromResult(ITestResult testResult) {
		Object currentClass = testResult.getInstance();
		return ((AppDriver) currentClass).getDriver();
	}

	public void ExtractChromeJSLogs(WebDriver driver) {
		logger.info("JS Errors");
		LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
		for (LogEntry entry : logEntries)
			logger.info(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
	}

	public void ExtractJSLogs(WebDriver driver, String driverType) throws MyCoreExceptions {
		ExtractDriverJSErrors(driver, getBrowserName(driverType));
	}

	@SuppressWarnings("incomplete-switch")
	private void ExtractDriverJSErrors(WebDriver driver, String browserName) {
		if (browserName.equalsIgnoreCase("ie")) {
			browserName = "INTERNET_EXPLORER";
		}
		switch (WebDriverConstants.BrowserNames.valueOf(browserName.toUpperCase())) {
		case CHROME:
			ExtractChromeJSLogs(driver);
			break;
		case FIREFOX:
			ExtractFFJSLogs(driver);
			break;
		}
	}

	private void ExtractFFJSLogs(WebDriver driver) {
		final List<JavaScriptError> jsErrors = JavaScriptError.readErrors(driver);
		logger.info(jsErrors.toString());
	}

	public String getBrowserToRun() {
		return System.getProperty("gridbrowser", System.getProperty("webdriver.browser", "chrome"))
				.toLowerCase(Locale.ENGLISH);
	}

	public String getEmulationDeviceName() {
		return System.getProperty("emulationDeviceName", "noEmul");// default "windows"
	}
}