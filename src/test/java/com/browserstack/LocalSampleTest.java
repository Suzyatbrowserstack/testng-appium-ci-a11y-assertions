package com.browserstack;

import static org.junit.Assert.*;

import java.time.Duration;
import org.junit.Test;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Test for LocalSample app (BasicNetworking)
 * Verifies BrowserStack Local connection by tapping the test button
 * and asserting the result text shows wifi connection is active.
 */
public class LocalSampleTest extends BrowserStackJUnitTest {

    @Test
    public void testLocalConnectionIsActive() throws Exception {

        // Step 1: Wait for and click the "Test Browserstack Local Connection" button
        WebElement testButton = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.elementToBeClickable(
                AppiumBy.id("com.example.android.basicnetworking:id/test_action")));
        assertNotNull("Test button should be visible on home screen", testButton);
        testButton.click();

        // Step 2: Wait for the result TextView to populate with a response
        WebElement resultText = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("com.example.android.basicnetworking:id/textView")));
        assertNotNull("Result text view should be present", resultText);

        // Step 3: Assert the connection result contains expected strings
        String result = resultText.getText();
        assertNotNull("Result text should not be null", result);
        assertTrue("Result should indicate active wifi connection",
            result.contains("The active connection is wifi"));
        assertTrue("Result should confirm service is up and running",
            result.contains("Up and running"));
    }
}