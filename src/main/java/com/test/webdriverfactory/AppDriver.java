package com.test.webdriverfactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestListenerAdapter;
import org.testng.annotations.AfterClass;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.test.constants.WebDriverConstants;
import com.test.customexception.MyCoreExceptions;
import com.test.utils.CommonUtils;
import com.test.webdriverhelpers.BaseDriverHelper;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

public class AppDriver extends TestListenerAdapter {

	protected static Logger logger = LoggerFactory.getLogger(AppDriver.class.getName());

	private final static String SKIP_EXCEPTION_MESSAGE = "Expected skip.";

	public static ExtentReports extent;
	public static ExtentTest test;

	public static ExtentSparkReporter spark;
	public static String reportDestinationPath;

	BaseDriverHelper baseDriverHelper = new BaseDriverHelper();
	CommonUtils utils = new CommonUtils();
	Set<String> skippedMethods = new HashSet<String>();

	private long startTime;
	private long stopTime;

	public long getStopTime() {
		return this.stopTime;
	}

	public long getStartTime() {
		return this.startTime;
	}

	private DriverManagerType browserType;

	public void setDriverExecutable() {
		for (DriverManagerType browser : DriverManagerType.values()) {
			if (browser.toString().toLowerCase().contains(
					System.getProperty("gridbrowser", WebDriverConstants.DEFAULT_BROWSER_NAME).toLowerCase())) {
				browserType = browser;
				break;
			} else if (System.getProperty("gridbrowser", WebDriverConstants.DEFAULT_BROWSER_NAME).toLowerCase()
					.equalsIgnoreCase("ie")
					|| System.getProperty("gridbrowser", WebDriverConstants.DEFAULT_BROWSER_NAME).toLowerCase()
							.equalsIgnoreCase("internetexplorer")) {
				browserType = DriverManagerType.IEXPLORER;
				break;
			}
		}
		String workingDir = utils.getCurrentWorkingDirectory();
		System.setProperty("wdm.targetPath", workingDir + "/resources/drivers/");
		if (System.getProperty("cleardriver", "false").equalsIgnoreCase("true")) {
			WebDriverManager.getInstance(browserType).clearDriverCache();
			WebDriverManager.getInstance(browserType).clearResolutionCache();
		}
		if (browserType.equals(DriverManagerType.IEXPLORER))
			WebDriverManager.getInstance(browserType).arch32();
		WebDriverManager.getInstance(browserType).setup();

	}

	public WebDriver getDriver() {
		try {
			logger.info("Checking driver..");
			if (baseDriverHelper.getDriver() == null) {
				baseDriverHelper.startServer();
				baseDriverHelper.startDriver();
			} else {
				logger.info("Driver already running..");
			}
		} catch (Exception e) {
			logger.info("Checking driver exception..");
			e.printStackTrace();
		}
		return baseDriverHelper.getDriver();
	}

	public boolean hasDriver() {
		return baseDriverHelper.getDriver() == null ? false : true;
	}

	public String getPrimaryWinhandle() throws MyCoreExceptions {
		return baseDriverHelper.getPrimaryWinhandle();
	}

	public static Logger getLogger() {
		return logger;
	}

	public Logger getLogger(Class<?> className) {
		Logger newLogger = baseDriverHelper.getLogger(className);
		if (newLogger != null)
			return newLogger;
		else {
			logger.warn("Logger initialization with class name provided failed. Returning default logger");
			return logger;
		}
	}

	public String getBrowserName() {
		return getDriver() != null ? ((RemoteWebDriver) getDriver()).getCapabilities().getBrowserName() : null;
	}

	@Override
	public void onStart(ITestContext context) {
		logger.info("Executing the Test in XML: " + context.getName());
		if (!Boolean.valueOf(System.getProperty("grid", "false").toLowerCase(Locale.ENGLISH)))
			reportDestinationPath = "Reports/Test" + CommonUtils.getCurrentTimeString() + "/";
		else
			reportDestinationPath = "target/Reports/";

		spark = new ExtentSparkReporter(reportDestinationPath);
		spark.config().setTheme(Theme.STANDARD);

		extent = new ExtentReports();
		extent.attachReporter(spark);

		startTime = System.currentTimeMillis();

	}

	@Override
	public void onFinish(ITestContext context) {

	}

	@Override
	public void onTestStart(ITestResult result) {

		String testName = result.getMethod().getMethodName();
		test = extent.createTest(testName);
		test.assignCategory(result.getInstanceName());
		test.log(Status.INFO, "DESCRIPTION :: " + result.getMethod().getDescription());
		ITestNGMethod testMethod = result.getMethod();

		String testMName = result.getMethod().getMethodName() + " - " + result.getTestClass().getName();
		logger.info("Starting the test : " + testMName);
		logger.info("Groups Depends on : " + testMethod.getGroupsDependedUpon() + "\n Methods Depends on : "
				+ testMethod.getMethodsDependedUpon());

	}

	@Override
	public void onTestFailure(ITestResult testResult) {
		try {
			logger.info("Test : " + testResult.getName() + "' FAILED");
			if (!(testResult.getThrowable() instanceof NoSuchWindowException
					|| testResult.getThrowable() instanceof NoSuchFrameException)) {
				String imageName = processResults(testResult, true);
				test.fail(testResult.getThrowable(), MediaEntityBuilder.createScreenCaptureFromPath(imageName).build());
				extent.flush();
			}
		} catch (MyCoreExceptions | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTestSuccess(ITestResult testResult) {
		try {
			String testName = testResult.getName();
			processResults(testResult, false);
			test.pass("Test : " + testName + "' PASSED.");
			extent.flush();
			logger.info("Test : " + testResult.getMethod().getMethodName() + " - " + testResult.getTestClass().getName()
					+ "' PASSED");
			logger.info("Test : " + testResult.getName() + "' PASSED");
		} catch (MyCoreExceptions e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTestSkipped(ITestResult testResult) {
		try {
			String testName = testResult.getName();
			processResults(testResult, false);
			test.skip("Test : " + testName + "' Skipped.");
			extent.flush();
			logger.info("Test : " + testResult.getMethod().getMethodName() + " - " + testResult.getTestClass().getName()
					+ "' Skipped");
			logger.info("Test : " + testResult.getName() + "' SKIPPED");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String processResults(ITestResult testResult, boolean takeScreenShot) throws MyCoreExceptions {
		Map<String, WebDriver> drivers = getDriverfromResult(testResult);
		String imageName = null;
		for (String driverType : drivers.keySet()) {
			long threadId = Thread.currentThread().getId();
			if (takeScreenShot) {
				utils.captureBrowserScreenShot(testResult.getName(), drivers.get(driverType));
				imageName = "FullSS_" + testResult.getName() + "_thread" + threadId + ".png";
				utils.captureFullBrowserScreenShotForExtent(reportDestinationPath + "/" + imageName,
						drivers.get(driverType));
			}

		}
		return imageName;

	}

	public Map<String, WebDriver> getDriverfromResult(ITestResult testResult) {
		Map<String, WebDriver> driverList = new HashMap<String, WebDriver>();
		AppDriver appDriver = getAppDriver(testResult);
		if (appDriver != null) {
			if (appDriver.hasDriver())
				driverList.put("primary", appDriver.getDriver());
		}

		return driverList;
	}

	protected AppDriver getAppDriver(ITestResult testResult) {
		Object currentClass = testResult.getInstance();
		if (currentClass instanceof AppDriver)
			return ((AppDriver) currentClass);
		else
			return null;
	}

	public void skipTest(String message) {
		throw new SkipException(SKIP_EXCEPTION_MESSAGE + message);
	}

	public void skipTest() {
		skipTest(" Note: No additional skip message was provided.\n");
	}

	protected boolean isExpectedSkip(ITestResult testResult) {
		Throwable thr = testResult.getThrowable();
		boolean flag = false;
		if (thr.getMessage().startsWith(SKIP_EXCEPTION_MESSAGE)) {
			flag = true;
		} else {
			for (String methodDependentUpon : testResult.getMethod().getMethodsDependedUpon()) {
				if (skippedMethods.contains(methodDependentUpon)) {
					flag = true;
					break;
				}
			}
		}
		if (flag) {
			String className = testResult.getMethod().getConstructorOrMethod().getMethod().getDeclaringClass()
					.getName();
			skippedMethods.add(className + "." + testResult.getMethod().getMethodName());
		}
		return flag;
	}

	@AfterClass(alwaysRun = true)
	public void afterClass() {
		logger.info("Stopping BaseDrivers");
		stopDriver();
		baseDriverHelper.stopServer();
	}

	public void stopDriver() {
		logger.info("Stopping driver..");
		baseDriverHelper.stopDriver();
	}

	protected void stopPrimaryDriver() {
		baseDriverHelper.stopPrimaryDriver();
	}

	public void setDriver(WebDriver driver) {
		baseDriverHelper.setDriver(driver);
	}

}