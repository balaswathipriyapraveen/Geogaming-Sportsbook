package support;

import org.openqa.selenium.WebDriver;

public class DriverManager {
    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();

    public static void setDriver(WebDriver driver) {
        TL_DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        WebDriver d = TL_DRIVER.get();
        if (d == null) throw new IllegalStateException("WebDriver not initialized. Hooks did not run?");
        return d;
    }

    public static void removeDriver() {
        TL_DRIVER.remove();
    }
}
