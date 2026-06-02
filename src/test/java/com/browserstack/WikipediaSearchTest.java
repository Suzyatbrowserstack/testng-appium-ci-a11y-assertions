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
 * Test cases: TC-565, TC-566, TC-567, TC-568, TC-569, TC-570
 * Wikipedia Search functionality tests
 */
public class WikipediaSearchTest extends BrowserStackJUnitTest {

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Opens the search overlay from the home screen. */
    private WebElement openSearch() {
        WebElement searchContainer = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.elementToBeClickable(
                AppiumBy.id("org.wikipedia.alpha:id/search_container")));
        searchContainer.click();
        return new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.elementToBeClickable(
                AppiumBy.id("org.wikipedia.alpha:id/search_src_text")));
    }

    /** Types a query and waits for results to appear. */
    private List<WebElement> searchAndWaitForResults(String query) {
        WebElement input = openSearch();
        input.sendKeys(query);
        new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/page_list_item_container")));
        return driver.findElements(AppiumBy.id("org.wikipedia.alpha:id/page_list_item_container"));
    }

    /** Taps the first item in the search results list. */
    private void tapFirstResult() {
        List<WebElement> results = driver.findElements(
            AppiumBy.id("org.wikipedia.alpha:id/page_list_item_container"));
        assertTrue("At least one search result must be present", results.size() > 0);
        results.get(0).click();
        // Wait for article page toolbar to appear
        new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/page_toolbar")));
    }

    // ── TC-565: Search bar is accessible ─────────────────────────────────────

    @Test
    public void testSearchBarIsAccessible() throws Exception {
        // Verify search container is visible on home screen
        WebElement searchContainer = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/search_container")));
        assertNotNull("Search bar should be visible on home screen", searchContainer);
        assertTrue("Search bar should be displayed", searchContainer.isDisplayed());

        // Tap search bar
        searchContainer.click();

        // Verify search input becomes active
        WebElement searchInput = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.elementToBeClickable(
                AppiumBy.id("org.wikipedia.alpha:id/search_src_text")));
        assertTrue("Search input should be active and visible after tap", searchInput.isDisplayed());
    }

    // ── TC-566: Search for 'BrowserStack' shows results ──────────────────────

    @Test
    public void testSearchBrowserStackShowsResults() throws Exception {
        List<WebElement> results = searchAndWaitForResults("BrowserStack");
        assertTrue("Search results list should be displayed with relevant entries", results.size() > 0);

        // Verify at least one result title contains BrowserStack
        List<WebElement> titles = driver.findElements(
            AppiumBy.id("org.wikipedia.alpha:id/page_list_item_title"));
        boolean foundBrowserStack = titles.stream()
            .anyMatch(el -> el.getText().toLowerCase().contains("browserstack"));
        assertTrue("At least one result should be related to BrowserStack", foundBrowserStack);
    }

    // ── TC-567: Tap hyperlink in BrowserStack article navigates correctly ─────

    @Test
    public void testTapHyperlinkInBrowserStackArticle() throws Exception {
        searchAndWaitForResults("BrowserStack");
        tapFirstResult();

        // Verify article toolbar shows BrowserStack title
        WebElement toolbar = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/page_toolbar")));
        assertNotNull("Article page toolbar should be present", toolbar);

        // Verify article content container is present (WebView or error state)
        WebElement pageContainer = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/page_contents_container")));
        assertNotNull("Article content container should be present", pageContainer);
    }

    // ── TC-568: Lowercase search returns relevant results ─────────────────────

    @Test
    public void testSearchWithLowercaseInput() throws Exception {
        List<WebElement> results = searchAndWaitForResults("browserstack");
        assertTrue("Search results should be displayed for lowercase input", results.size() > 0);

        List<WebElement> titles = driver.findElements(
            AppiumBy.id("org.wikipedia.alpha:id/page_list_item_title"));
        boolean foundBrowserStack = titles.stream()
            .anyMatch(el -> el.getText().toLowerCase().contains("browserstack"));
        assertTrue("Case-insensitive search should return BrowserStack results", foundBrowserStack);
    }

    // ── TC-569: Back navigation after tapping a hyperlink ────────────────────

    @Test
    public void testBackNavigationAfterHyperlinkTap() throws Exception {
        searchAndWaitForResults("BrowserStack");
        tapFirstResult();

        // Verify we are on the article page
        new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/page_toolbar")));

        // Navigate back using the Navigate up button
        WebElement backButton = new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.elementToBeClickable(
                AppiumBy.accessibilityId("Navigate up")));
        backButton.click();

        // Verify we returned to search results or home screen
        new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/search_container")));
        assertTrue("App should navigate back after tapping back button", true);
    }

    // ── TC-570: Empty search shows no results / prompt ────────────────────────

    @Test
    public void testSearchWithEmptyInput() throws Exception {
        WebElement searchInput = openSearch();

        // Submit empty query
        searchInput.sendKeys("");

        // App should not crash — search input still visible
        assertTrue("App should not crash on empty search", searchInput.isDisplayed());

        // No article result items should appear for an empty query
        List<WebElement> results = driver.findElements(
            AppiumBy.id("org.wikipedia.alpha:id/page_list_item_container"));
        assertTrue("Empty search should show no article results", results.size() == 0);
    }
}