package com.test.webdriverfactory;

import java.util.HashMap;
import java.util.Map;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.test.utils.CommonUtils;

public class ExtentTestManager {
	static Map<Integer, ExtentTest> extentTestMap = new HashMap<Integer, ExtentTest>();
	static ExtentReports extent = new ExtentReports();
	static String reportDestinationPath = "Reports/Test" + CommonUtils.getCurrentTimeString() + "/";
	static ExtentSparkReporter spark = new ExtentSparkReporter(ExtentTestManager.reportDestinationPath);
	static ExtentTest test;

	public static synchronized ExtentTest getTest() {
		return (ExtentTest) extentTestMap.get((int) (long) (Thread.currentThread().getId()));
	}

	public static synchronized void endTest() {
		extent.flush();
	}

	public static synchronized ExtentTest startTest(String testName) {
		ExtentTest test = extent.createTest(testName);
		extentTestMap.put((int) (long) (Thread.currentThread().getId()), test);
		return test;
	}
}
