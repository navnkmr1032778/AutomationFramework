package com.test.webdriverhelpers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.utils.CommonUtils;

import io.appium.java_client.remote.MobileBrowserType;
import net.jsourcerer.webdriver.jserrorcollector.JavaScriptError;

public class SetBrowserCapabilities {

	CommonUtils utils = new CommonUtils();
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public boolean isGridRun() {
		return Boolean.valueOf(System.getProperty("grid", "false").toLowerCase(Locale.ENGLISH));
	}

	public boolean ismobile() {
		return Boolean.valueOf(System.getProperty("ismobile", "false").toLowerCase(Locale.ENGLISH));

	}

	public String getBrowserToRun() {
		return System.getProperty("gridbrowser", System.getProperty("webdriver.browser", "chrome"))
				.toLowerCase(Locale.ENGLISH);
	}

	public String getMobilePlatform() {
		logger.info("platform: " + System.getProperty("mobileplatform") + " end");
		return System.getProperty("mobileplatform");
	}

	public String getMobileName() {
		logger.info(System.getProperty("mobilename", "noDevice"));
		return System.getProperty("mobilename");
	}

	public String getMobilePlatformVersion() {
		logger.info(System.getProperty("mobileplatformversion").toLowerCase(Locale.ENGLISH));
		return System.getProperty("mobileplatformversion").toLowerCase(Locale.ENGLISH);
	}

	private String getMobileBrowserName(String browserStr) {
		String browserName = MobileBrowserType.BROWSER;
		if (browserStr.equalsIgnoreCase("chrome")) {
			browserName = MobileBrowserType.CHROME;
		} else if (browserStr.equalsIgnoreCase("chromium")) {
			browserName = MobileBrowserType.CHROMIUM;
		} else if (browserStr.equalsIgnoreCase("safari")) {
			browserName = MobileBrowserType.SAFARI;
		}
		return browserName;
	}

	public ChromeOptions setChromeDriver() {
		ChromeOptions cap = new ChromeOptions();
		try {
			String workingDir = utils.getCurrentWorkingDirectory();

			if (workingDir == null) {
				logger.info("Working directory is Null");
				return null;
			}
//   			
//   			if(!isGridRun())
//   			{
//   				File chromeDriver = utils.getBrowserExecutable((workingDir+WebDriverConstants.PATH_TO_BROWSER_EXECUTABLE), "chrome");
//   	   		    
//   	   			if(chromeDriver.getName().equals("tempfile"))
//   	   			{
//   	   				logger.warn("Unable to find executable file");
//   	   				return null;
//   	   			}
//   	   			System.setProperty("webdriver.chrome.driver", chromeDriver.getAbsolutePath());
//   			}

			cap = new ChromeOptions();
			LoggingPreferences loggingprefs = new LoggingPreferences();
			loggingprefs.enable(LogType.BROWSER, Level.ALL);
			loggingprefs.enable(LogType.DRIVER, Level.ALL);
			cap.setCapability(CapabilityType.LOGGING_PREFS, loggingprefs);
			cap.addArguments("--disable-extensions");
			cap.addArguments("--session-override=true");
			cap.setAcceptInsecureCerts(true);
			String windowSize = System.getProperty("windowSize", "");

			if (windowSize.matches("^\\d+,\\d+$")) {
				cap.addArguments("--window-size=" + windowSize);
			}

			String emulDeviceName = System.getProperty("emulationDeviceName", "noEmul");
			if (!emulDeviceName.equals("noEmul")) {
				Map<String, String> mobileEmulation = new HashMap<String, String>();
				mobileEmulation.put("deviceName", emulDeviceName);
				Map<String, Object> chromeOptions = new HashMap<String, Object>();
				chromeOptions.put("mobileEmulation", mobileEmulation);
				cap.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
			} else {
				cap.setCapability(ChromeOptions.CAPABILITY, cap);
			}

			if (ismobile()) {
				logger.info("setting mobile driver capabilities");
				try {
					cap = new ChromeOptions();
					String mobilePlatform = getMobilePlatform();
					String mobileName = getMobileName();
					String browserName = getMobileBrowserName(getBrowserToRun());
					String platformVersion = getMobilePlatformVersion();
					logger.info("desired capabilities for mobile " + mobilePlatform + " " + platformVersion + " "
							+ mobileName + " " + browserName);
					cap.setCapability("platformName", mobilePlatform);// Android
					cap.setCapability("platformVersion", platformVersion);// 5.0.1
					cap.setCapability("deviceName", mobileName);
					cap.setCapability("browserName", browserName);

				} catch (Exception ex) {
					ex.printStackTrace();
					logger.info("exception raised in mobile desired capabilities");
				}
			}
			cap.setCapability(CapabilityType.BROWSER_NAME, "chrome");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cap;
	}

	public FirefoxOptions setFirefoxDriver() {
		FirefoxOptions cap = new FirefoxOptions();
		try {
			final FirefoxProfile profile = new FirefoxProfile();
			JavaScriptError.addExtension(profile);
			cap.setCapability(FirefoxDriver.PROFILE, profile);
			cap.setCapability(CapabilityType.BROWSER_NAME, "firefox");
			if (ismobile()) {
				logger.info("setting mobile driver capabilities");
				try {
					cap = new FirefoxOptions();
					String mobilePlatform = getMobilePlatform();
					String mobileName = getMobileName();
					String browserName = getMobileBrowserName(getBrowserToRun());
					String platformVersion = getMobilePlatformVersion();
					logger.info("desired capabilities for mobile " + mobilePlatform + " " + platformVersion + " "
							+ mobileName + " " + browserName);
					cap.setCapability("platformName", mobilePlatform);// Android
					cap.setCapability("platformVersion", platformVersion);// 5.0.1
					cap.setCapability("deviceName", mobileName);
					cap.setCapability("browserName", browserName);

				} catch (Exception ex) {
					ex.printStackTrace();
					logger.info("exception raised in mobile desired capabilities");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cap;
	}

	public InternetExplorerOptions setIEDriver() {
		InternetExplorerOptions cap = new InternetExplorerOptions();
		try {
			String workingDir = utils.getCurrentWorkingDirectory();
			if (workingDir == null) {
				logger.warn("Working directory is Null ");
				return null;
			}

//   			if(!isGridRun())
//   			{
//   				File ieDriver = utils.getBrowserExecutable((workingDir+WebDriverConstants.PATH_TO_BROWSER_EXECUTABLE), "IE");
//   	   		    
//   	   			if(ieDriver.getName().equals("tempfile"))
//   	   			{
//   	   				logger.info("Unable to find executable file");
//   	   				return null;
//   	   			}
//   	   			System.setProperty("webdriver.ie.driver", ieDriver.getAbsolutePath());
//   			}
			cap.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			cap.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
			cap.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
			cap.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, false);
			cap.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
			cap.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
			cap.setCapability(InternetExplorerDriver.ENABLE_ELEMENT_CACHE_CLEANUP, false);
			cap.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);
			cap.setCapability(InternetExplorerDriver.IE_SWITCHES, "-private");
			cap.setCapability("javascriptEnabled", true);
			cap.setCapability(CapabilityType.BROWSER_NAME, "internet explorer");
			if (ismobile()) {
				logger.info("setting mobile driver capabilities");
				try {
					cap = new InternetExplorerOptions();
					String mobilePlatform = getMobilePlatform();
					String mobileName = getMobileName();
					String browserName = getMobileBrowserName(getBrowserToRun());
					String platformVersion = getMobilePlatformVersion();
					logger.info("desired capabilities for mobile " + mobilePlatform + " " + platformVersion + " "
							+ mobileName + " " + browserName);
					cap.setCapability("platformName", mobilePlatform);// Android
					cap.setCapability("platformVersion", platformVersion);// 5.0.1
					cap.setCapability("deviceName", mobileName);
					cap.setCapability("browserName", browserName);

				} catch (Exception ex) {
					ex.printStackTrace();
					logger.info("exception raised in mobile desired capabilities");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cap;
	}

	/*
	 * public DesiredCapabilities setPhomtomJsDriver( DesiredCapabilities cap) {
	 * try{ String workingDir = utils.getCurrentWorkingDirectory(); if(workingDir ==
	 * null) { logger.warn("Working directory is Null "); return null; }
	 * 
	 * if(!isGridRun()) { File phantomDriver =
	 * utils.getBrowserExecutable((workingDir+WebDriverConstants.
	 * PATH_TO_BROWSER_EXECUTABLE), "phantomjs");
	 * 
	 * if(phantomDriver.getName().equals("tempfile")) {
	 * logger.info("Unable to find executable file"); return null; }
	 * System.setProperty("phantomjs.binary.path", phantomDriver.getAbsolutePath());
	 * }
	 * 
	 * cap = DesiredCapabilities.phantomjs();
	 * 
	 * }catch(Exception e){ e.printStackTrace(); } return cap; }
	 */
}