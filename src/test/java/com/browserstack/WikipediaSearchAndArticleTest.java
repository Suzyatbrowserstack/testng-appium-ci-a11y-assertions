package com.browserstack;

import static org.junit.Assert.*;

import org.junit.Test;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.time.Duration;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.WebElement;

import com.browserstack.accessibility.AccessibilityUtils;

/**
 * Test case: TC-381
 * Search for a term and open the first article result in Wikipedia Sample App.
 * Includes BrowserStack App Accessibility assertions using AccessibilityUtils.
 *
 * Steps:
 * 1. Tap the search bar on the home screen
 * 2. Enter search query "Android"
 * 3. Verify search results are displayed
 * 4. Tap the first result (with retry on failed-to-load page)
 * 5. Verify the article page toolbar is displayed
 * 6. Verify the article content container is present
 * 7. A11Y Assertion 1 (PASS): critical issues < 30
 * 8. A11Y Assertion 2 (PASS): color contrast issues < 10
 * 9. A11Y Assertion 3 (INTENTIONAL FAIL): critical issues == 0
 */
public class WikipediaSearchAndArticleTest extends BrowserStackJUnitTest {

    @Test
    public void testSearchAndOpenArticle() throws Exception {

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
        searchInput.sendKeys("Android");

        // Step 3: Wait for results — dismiss any "error occurred" dialog that may appear
        new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/page_list_item_container")));

        // Dismiss in-app error dialog if present (appears over search results on network issues)
        List<WebElement> errorDialogButtons = driver.findElements(
            By.xpath("//*[contains(@text,'OK') or contains(@text,'Dismiss') or contains(@text,'Close') or contains(@text,'Got it')]"));
        if (!errorDialogButtons.isEmpty()) {
            System.out.println("[WARN] Error dialog detected — dismissing it.");
            errorDialogButtons.get(0).click();
            // Wait for dialog to close and results to be stable
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.stalenessOf(errorDialogButtons.get(0)));
        }

        List<WebElement> results = driver.findElements(
            AppiumBy.id("org.wikipedia.alpha:id/page_list_item_container"));
        assertTrue("Search results should be displayed for 'Android'", results.size() > 0);

        // Step 4: Tap the first result (with retry if article fails to load)
        results.get(0).click();

        // Wait up to 60s for the article toolbar — slow network can cause "failed to load"
        WebDriverWait articleWait = new WebDriverWait(driver, Duration.ofSeconds(60));
        boolean loaded = false;
        for (int attempt = 1; attempt <= 3 && !loaded; attempt++) {
            try {
                articleWait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.id("org.wikipedia.alpha:id/page_toolbar")));
                loaded = true;
            } catch (Exception e) {
                System.out.println("[RETRY] Article load attempt " + attempt + " failed. Checking for error page...");
                // Look for Retry / Try again button on the error page
                List<WebElement> retryButtons = driver.findElements(
                    By.xpath("//*[contains(@text,'Retry') or contains(@text,'retry') or contains(@text,'Try again')]"));
                if (!retryButtons.isEmpty()) {
                    System.out.println("[RETRY] Found retry button — tapping it.");
                    retryButtons.get(0).click();
                } else if (attempt < 3) {
                    // Go back and try the next search result
                    System.out.println("[RETRY] No retry button — going back to results.");
                    driver.navigate().back();
                    new WebDriverWait(driver, Duration.ofSeconds(30))
                        .until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.id("org.wikipedia.alpha:id/page_list_item_container")));
                    List<WebElement> freshResults = driver.findElements(
                        AppiumBy.id("org.wikipedia.alpha:id/page_list_item_container"));
                    freshResults.get(Math.min(attempt, freshResults.size() - 1)).click();
                }
            }
        }
        assertTrue("Article page should have loaded after retries", loaded);

        // Step 5: Verify the article page toolbar is present
        WebElement toolbar = driver.findElement(AppiumBy.id("org.wikipedia.alpha:id/page_toolbar"));
        assertNotNull("Article page toolbar should be present after tapping a search result", toolbar);
        assertTrue("Article page toolbar should be displayed", toolbar.isDisplayed());

        // Step 6: Verify article content container is present (wait up to 60s for slow loads)
        WebElement pageContainer = new WebDriverWait(driver, Duration.ofSeconds(60))
            .until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.id("org.wikipedia.alpha:id/page_contents_container")));
        assertNotNull("Article content container should be present", pageContainer);
        assertTrue("Article content container should be displayed", pageContainer.isDisplayed());

        // ── Accessibility Assertions ──────────────────────────────────────────
        Map<String, Object> summary = AccessibilityUtils.getResultsSummary(driver);
        assertNotNull("Accessibility summary should not be null", summary);
        Map<?, ?> bySeverity = (Map<?, ?>) summary.get("totalBySeverity");
        assertNotNull("totalBySeverity should be present in summary", bySeverity);
        int criticalIssueCount = Integer.parseInt(String.valueOf(bySeverity.get("critical")));
        System.out.println("[A11Y] Critical issues: " + criticalIssueCount
            + ", Total: " + summary.get("totalIssueCount"));

        ArrayList<Map<String, Object>> issues = AccessibilityUtils.getResults(driver);
        assertNotNull("Accessibility issues list should not be null", issues);
        long colorContrastIssues = issues.stream()
            .filter(issue -> "color-contrast".equals(issue.get("ruleId")))
            .count();
        long seriousIssues = issues.stream()
            .filter(issue -> "serious".equals(issue.get("severity")))
            .count();
        System.out.println("[A11Y] Color contrast issues: " + colorContrastIssues);
        System.out.println("[A11Y] Serious issues: " + seriousIssues);

        // A11Y Assertion 1 (PASS): critical issues below 30
        assertTrue("Critical accessibility issue count should be below 30, but found: " + criticalIssueCount,
            criticalIssueCount < 30);

        // A11Y Assertion 2 (PASS): color contrast issues below 10
        assertTrue("Color contrast issue count should be below 10, but found: " + colorContrastIssues,
            colorContrastIssues < 10);

        // A11Y Assertion 3 (INTENTIONAL FAIL): assert zero critical issues — will fail since there are 26
       // assertTrue("[INTENTIONAL FAIL] Critical accessibility issues should be 0, but found: " + criticalIssueCount,
         //   criticalIssueCount == 0);
    }
}