package support;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class Waits {
    public static <T> T waitFor(WebDriver driver, int timeoutSec, Function<WebDriver, T> condition) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSec))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .until(condition);
    }

    public static WebElement firstPresent(WebDriver driver, int timeoutSec, By... locators) {
        return waitFor(driver, timeoutSec, d -> {
            for (By by : locators) {
                try {
                    WebElement el = d.findElement(by);
                    if (el.isDisplayed()) return el;
                } catch (Exception ignored) {}
            }
            return null;
        });
    }
}
