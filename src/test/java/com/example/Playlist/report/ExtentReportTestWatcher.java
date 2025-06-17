// ExtentReportTestWatcher.java
package com.example.Playlist.report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import org.junit.jupiter.api.extension.*;

public class ExtentReportTestWatcher implements TestWatcher, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    public static void setExtentReports(ExtentReports extent) {
        extentReports = extent;
    }

    public static ExtentTest getTest() {
        return testThread.get();
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        getTest().pass("✅ Test passed");
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        getTest().fail("❌ Test failed: " + cause.getMessage());
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        ExtentTest test = extentReports.createTest(context.getDisplayName());
        testThread.set(test);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        // Optional: clean up
    }
}
