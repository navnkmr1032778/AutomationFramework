package com.test.constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebDriverConstants {

	public static String PATH_TO_BROWSER_EXECUTABLE = "/resources/drivers/";
	public static String PATH_TO_BROWSER_SCREENSHOT = "resources/screenshot/";
	public static String PATH_TO_BROWSER_SCREENSHOT_BASE = "resources/screenshot/base";
	public static String PATH_TO_BROWSER_SCREENSHOT_COMPARE = "resources/screenshot/compare";
	public static String PATH_TO_BROWSER_SCREENSHOT_COMPARE_RESULT = "resources/screenshot/compare_result";
	public static String PATH_TO_TEST_DATA_FILE = "/resources/testdata/";
	public static String WINDOWS_PATH_TO_TEST_DATA_DIR = "/resources/testdata/";
	public static String DEFAULT_BROWSER_NAME = "chrome";
	public static int WAIT_TWO_MIN = 120;
	public static int MAX_TIMEOUT_PAGE_LOAD = 30;

	public enum OperatingSystem {
		WINDOWS, MAC
	}

	public enum DriverTypes {
		PRIMARY, SECONDARY
	}

	public enum BrowserNames {
		CHROME, FIREFOX, INTERNET_EXPLORER, PHANTOMJS
	}

	public static final Map<String, String> DRIVER_METHOD;
	static {
		Map<String, String> tmp = new LinkedHashMap<String, String>();
		tmp.put("ie", "setIEDriver");
		tmp.put("internet explorer", "setIEDriver");
		tmp.put("firefox", "setFirefoxDriver");
		tmp.put("chrome", "setChromeDriver");
		tmp.put("phantomjs", "setPhomtomJsDriver");
		DRIVER_METHOD = Collections.unmodifiableMap(tmp);
	}

}
