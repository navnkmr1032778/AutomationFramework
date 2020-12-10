package com.test.webdriverbase;

import java.util.Locale;

import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import com.test.utils.CommonUtils;
import com.test.webdriverfactory.AppDriver;

@Listeners(AppDriver.class)
public class AppTest extends AppDriver {

	CommonUtils utils = new CommonUtils();

	@BeforeSuite(alwaysRun = true)
	public void beforeSuite(ITestContext ctx) {
		if (!Boolean.valueOf(System.getProperty("grid", "false").toLowerCase(Locale.ENGLISH)))
			setDriverExecutable();
		logger.info("XML FileName : " + ctx.getCurrentXmlTest().getSuite().getFileName());
		logger.info("Executing the Suite : " + ctx.getSuite().getName());
	}

	@AfterSuite(alwaysRun = true)
	public void AfterSuite() {
	}

}
