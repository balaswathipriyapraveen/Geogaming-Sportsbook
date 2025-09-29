package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ResultsPage(WebDriver driver, int timeoutSec) {
        this.driver = driver;
        this.wait  = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
    }

    /* ---------------- Overlay-scoped locators ---------------- */

    /** ALL Angular CDK search panes (id like cdk-overlay-10). We’ll pick the visible/topmost one. */
    private static final By OVERLAY_PANES = By.cssSelector(
            ".cdk-overlay-container .cdk-overlay-pane.sports-search-panel"
    );

    /** Core controls inside the overlay */
    private static final By SEARCH_INPUT   = By.id("search-input");
    private static final By RESULTS_COUNT  = By.cssSelector(".search-results-count");
    private static final By NO_RESULTS_BOX = By.cssSelector(".search-no-results");
    private static final By CLEAR_X        = By.cssSelector(".search-input__icon--clear");

    /**
     * Result rows *inside the overlay only* and definitely visible.
     * Guard with :not(.search-dropdown__item--hidden) because the app keeps hidden buckets in DOM.
     */
    private static final By VISIBLE_RESULT_ROWS = By.cssSelector(
            ".search-dropdown__item:not(.search-dropdown__item--hidden)"
    );

    /** A more specific selector for the visible “no results” banner inside the active dropdown. */
    private static final By VISIBLE_NO_RESULTS = By.cssSelector(
            ".search-dropdown.search-dropdown--no-results:not(.search-dropdown__item--hidden) .search-no-results"
    );

    /* ---------------- Public API ---------------- */

    /** Wait for overlay + (rows OR count OR no-results) to appear, all overlay-scoped. */
    public void waitLoaded() {
        wait.until(drv -> activeOverlay() != null); // at least one visible pane exists

        ExpectedCondition<Boolean> overlayHasContent = drv -> {
            WebElement root = activeOverlay();
            if (root == null) return false;

            // Any of these is enough to say the overlay "loaded":
            if (isDisplayedInside(root, RESULTS_COUNT))        return true;          // e.g., "Search results (9)"
            if (hasAnyVisibleRow(root))                        return true;          // visible rows
            if (visibleNoResultsElement(root) != null)         return true;          // “There are no results...”
            return false;
        };
        wait.until(overlayHasContent);
    }

    /** True iff visible result rows exist in the overlay. */
    public boolean hasResultsRowsVisible() {
        WebElement root = activeOverlay();
        return root != null && hasAnyVisibleRow(root);
    }

    /** Wait for the friendly “no results” message in the current overlay. */
    public boolean waitNoResultsMessage(int timeoutSec) {
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
        try {
            WebElement banner = w.until(drv -> {
                WebElement root = activeOverlay();
                if (root == null) return null;

                // 1) Preferred: a visible banner inside an active "no-results" dropdown
                WebElement vis = visibleNoResultsElement(root);
                if (vis != null) return vis;

                // 2) Fallback: any visible .search-no-results under the active pane
                List<WebElement> any = root.findElements(NO_RESULTS_BOX);
                for (WebElement e : any) if (isDisplayed(e)) return e;

                // 3) Last resort: scan all panes (if overlay id flips between polls)
                for (WebElement pane : driver.findElements(OVERLAY_PANES)) {
                    if (!isDisplayed(pane)) continue;
                    List<WebElement> nr = pane.findElements(NO_RESULTS_BOX);
                    for (WebElement e : nr) if (isDisplayed(e)) return e;
                }
                return null;
            });

            String text = normalize(banner.getText());
            // e.g. "There are no results that match your search. Try again."
            // Allow minor whitespace/case differences.
            return text.contains("no results")
                    && text.contains("match your")
                    && text.contains("search");
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Wait for “Search History is empty” in the overlay (used by Scenario 3). */
    public boolean waitHistoryEmpty(int timeoutSec) {
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
        return Boolean.TRUE.equals(w.until(drv -> {
            WebElement root = activeOverlay();
            if (root == null) return false;
            for (WebElement box : root.findElements(NO_RESULTS_BOX)) {
                if (isDisplayed(box) && normalize(box.getText()).contains("history is empty")) {
                    return true;
                }
            }
            return false;
        }));
    }

    /** Click the ✖ and wait for the input to clear (or icon to vanish). */
    public void clickClearX() {
        WebElement x = wait.until(ExpectedConditions.elementToBeClickable(CLEAR_X));
        x.click();
        wait.until(drv -> {
            String v = getSearchBoxValue();
            boolean empty = v == null || v.isEmpty();
            boolean xGone = drv.findElements(CLEAR_X).isEmpty();
            return empty || xGone;
        });
    }

    public String getSearchBoxValue() {
        try {
            return driver.findElement(SEARCH_INPUT).getAttribute("value");
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    /** Parse N from “Search results (N)” (overlay-scoped). 0 if absent. */
    public int getResultCountVisible() {
        WebElement root = activeOverlay();
        if (root != null) {
            WebElement el = first(root, RESULTS_COUNT);
            if (isDisplayed(el)) {
                Matcher m = Pattern.compile("\\((\\d+)\\)").matcher(el.getText());
                if (m.find()) return Integer.parseInt(m.group(1));
            }
        }
        // fallback: any visible row?
        return hasResultsRowsVisible() ? 1 : 0;
    }

    /* ---------------- Helpers ---------------- */

    /** Find the *visible/topmost* overlay pane (there can be several; we use the last displayed). */
    private WebElement activeOverlay() {
        List<WebElement> panes = driver.findElements(OVERLAY_PANES);
        WebElement candidate = null;
        for (WebElement p : panes) {
            if (isDisplayed(p)) candidate = p;  // last displayed wins
        }
        return candidate;
    }

    private WebElement visibleNoResultsElement(WebElement root) {
        try {
            List<WebElement> els = root.findElements(VISIBLE_NO_RESULTS);
            for (WebElement e : els) if (isDisplayed(e)) return e;
        } catch (StaleElementReferenceException ignored) {}
        return null;
    }

    private boolean hasAnyVisibleRow(WebElement root) {
        try {
            List<WebElement> rows = root.findElements(VISIBLE_RESULT_ROWS);
            for (WebElement r : rows) {
                if (isDisplayed(r)) return true;
            }
        } catch (StaleElementReferenceException ignored) {}
        return false;
    }

    private boolean isDisplayedInside(WebElement root, By by) {
        WebElement el = first(root, by);
        return isDisplayed(el);
    }

    private boolean isDisplayed(WebElement el) {
        if (el == null) return false;
        try {
            return el.isDisplayed() && el.getSize().height > 0 && el.getSize().width > 0;
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    private WebElement first(WebElement scope, By by) {
        if (scope == null) return null;
        try { return scope.findElement(by); }
        catch (NoSuchElementException e) { return null; }
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.replace('\u00A0',' ') // NBSP → space
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
}
