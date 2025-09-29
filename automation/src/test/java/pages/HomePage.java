package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import support.Waits;

import java.util.Arrays;
import java.util.List;

public class HomePage {
    private final WebDriver driver;

    public HomePage(WebDriver driver) {
        this.driver = driver;
    }

    // Search input field
    private static final By SEARCH_INPUT = By.id("search-input");

    // ⬇⬇⬇ ADD: clear “X” icon inside the input
    private static final By CLEAR_X = By.cssSelector(".search-input__icon--clear");

    // Search trigger(s) - button or magnifier icon
    private static final List<By> SEARCH_TRIGGERS = Arrays.asList(
            By.cssSelector("span.search-button__text.search-button--sport"),
            By.cssSelector("button.search-button"),
            By.cssSelector(".spb-icon__svg") // magnifier SVG
    );

    // Overlay container (Angular CDK overlay opens search box)
    private static final By OVERLAY_ROOT = By.cssSelector(".cdk-overlay-container");

    // Cookie/consent buttons
    public static final List<By> COOKIE_ACCEPT_BUTTONS = Arrays.asList(
            By.cssSelector("button#onetrust-accept-btn-handler"),
            By.cssSelector("button[aria-label*='Accept']"),
            By.xpath("//button[contains(.,'Accept all') or contains(.,'Accept')]"),
            By.xpath("//button[contains(.,'I agree') or contains(.,'Agree')]"),
            By.xpath("//button[contains(.,'Allow')]")
    );

    public void open(String url) {
        if (!url.contains("/sportsbook")) {
            url = url.endsWith("/") ? url + "sportsbook" : url + "/sportsbook";
        }
        driver.get(url);

        sleep(1000);

        dismissCookiesIfPresent(8);

        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,0);");
        } catch (Exception ignored) {}
    }

    public void focusSearch(int timeoutSec) {
        // Ensure overlay is open
        openSearchOverlay(timeoutSec);

        WebElement input = Waits.waitFor(driver, timeoutSec,
                ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
        input.click();
    }

    public void typeQuery(String text, int t) {
        WebElement el = Waits.waitFor(driver, t,
                ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
        el.clear();
        el.sendKeys(text);
    }

    public void submitEnter(int t) {
        WebElement el = Waits.waitFor(driver, t,
                ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
        el.sendKeys(Keys.ENTER);
    }

    private void openSearchOverlay(int timeoutSec) {
        for (By trigger : SEARCH_TRIGGERS) {
            try {
                WebElement btn = Waits.firstPresent(driver, 2, trigger);
                if (btn != null && isInteractable(btn)) {
                    btn.click();
                    Waits.waitFor(driver, timeoutSec,
                            ExpectedConditions.presenceOfElementLocated(OVERLAY_ROOT));
                    return;
                }
            } catch (Exception ignored) {}
        }
        throw new NoSuchElementException("Search trigger not found in header.");
    }

    private void dismissCookiesIfPresent(int timeoutSec) {
        long end = System.currentTimeMillis() + timeoutSec * 1000L;
        for (By by : COOKIE_ACCEPT_BUTTONS) {
            try {
                WebElement b = Waits.firstPresent(driver, 1, by);
                if (b != null && b.isDisplayed()) {
                    Waits.waitFor(driver, 2, ExpectedConditions.elementToBeClickable(b));
                    b.click();
                    sleep(300);
                    return;
                }
            } catch (Exception ignored) {}
            if (System.currentTimeMillis() > end) break;
        }
        try {
            driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
        } catch (Exception ignored) {}
    }

    private boolean isInteractable(WebElement el) {
        try {
            return el.isDisplayed() && el.isEnabled()
                    && el.getSize().height > 0 && el.getSize().width > 0;
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    /* -----------------  ⬇⬇⬇ ADD for TC-8  ----------------- */

    /** Clicks the “X” icon to clear the search and waits until the input becomes empty. */
    public void clearSearchViaX(int t) {
        // Ensure overlay/input visible
        focusSearch(Math.max(3, t));
        WebElement x = Waits.waitFor(driver, t, ExpectedConditions.elementToBeClickable(CLEAR_X));
        x.click();
        // Wait until value is empty
        Waits.waitFor(driver, t, d -> getSearchValue().isEmpty());
    }

    /** Returns the current value in the search input (empty string if not present). */
    public String getSearchValue() {
        try {
            WebElement input = driver.findElement(SEARCH_INPUT);
            String v = input.getAttribute("value");
            return v == null ? "" : v;
        } catch (NoSuchElementException e) {
            return "";
        }
    }
}
