package com.browserstack;

import static org.junit.Assert.*;

import org.junit.Test;
import java.util.List;
import java.time.Duration;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.WebElement;

/**
 * Test case: TC-380
 * Simple search and tap hyperlink test for Wikipedia Sample App.
 *
 * Steps:
 * 1. Search for "BrowserStack"
 * 2. Tap the first result to open the article
 * 3. Verify the article page loads with a toolbar
 */
public class WikipediaSearchAndHyperlinkTest extends BrowserStackJUnitTest {

    @Test
    public void testSearchAndTapHyperlink() throws Exception {

        // Step 1: Tap the search bar on the home screen
        WebElement searchContainer = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.elementToBeClickable(
                AppiumBy.id("org.wikipedia.alpha:id/search_container")));
        assertNotNull("Search bar should be visible on home screen", searchContainer);
        searchContainer.click();

        // Step 2: Enter search query
        WebElement searchInput = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.elementToBeClickable(
                AppiumBy.id("org.wikipedia.alpha:id/search_src_text")));
        searchInput.sendKeys("BrowserStack");

        // Step 3: Wait for results and verify at least one is returned
        new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/page_list_item_container")));
        List<WebElement> results = driver.findElements(
            AppiumBy.id("org.wikipedia.alpha:id/page_list_item_container"));
        assertTrue("Search results should be displayed", results.size() > 0);

        // Step 4: Tap the first result (hyperlink)
        results.get(0).click();

        // Step 5: Verify the article page loads
        WebElement toolbar = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/page_toolbar")));
        assertNotNull("Article page toolbar should be present after tapping hyperlink", toolbar);
        assertTrue("Article page should be displayed", toolbar.isDisplayed());

        // Step 6: Verify article content container is present
        WebElement pageContainer = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/page_contents_container")));
        assertNotNull("Article content container should be present", pageContainer);
        assertTrue("Article content should be displayed", pageContainer.isDisplayed());
    }
}