package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import support.Waits;

import java.util.List;

public class SearchSuggestions {
    private final WebDriver driver;
    private final int t;

    public SearchSuggestions(WebDriver d, int timeout) {
        this.driver = d;
        this.t = timeout;
    }

    private By[] panel = new By[]{
            By.cssSelector("[role='listbox']"),
            By.cssSelector(".autocomplete, .typeahead, .suggestions")
    };

    private By[] items = new By[]{
            By.cssSelector("[role='listbox'] [role='option']"),
            By.cssSelector(".suggestions li, .autocomplete li, .typeahead li")
    };

    public boolean isVisible() {
        WebElement p = Waits.firstPresent(driver, t, panel);
        return p != null && p.isDisplayed();
    }

    public void clickFirst() {
        WebElement container = Waits.firstPresent(driver, t, panel);
        if (container == null) throw new NoSuchElementException("Suggestions panel not visible.");
        List<WebElement> list = driver.findElements(items[0]);
        if (list.isEmpty()) throw new NoSuchElementException("No suggestion items found.");
        Waits.waitFor(driver, t, ExpectedConditions.elementToBeClickable(list.get(0)));
        list.get(0).click();
    }
}

