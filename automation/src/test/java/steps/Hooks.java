package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import support.DriverManager;

public class Hooks {

    @Before
    public void beforeScenario() {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        DriverManager.setDriver(driver);
    }

    @After
    public void afterScenario() {
        WebDriver driver = DriverManager.getDriver();
        try {
            if (driver != null) driver.quit();
        } finally {
            DriverManager.removeDriver();
        }
    }
}
