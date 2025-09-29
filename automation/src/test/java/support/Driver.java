package support;

import org.openqa.selenium.WebDriver;

public class Driver {
    private static WebDriver driver;

    public static WebDriver get() {
        return driver;
    }

    public static void set(WebDriver d) {
        driver = d;
    }

    public static void quit() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
