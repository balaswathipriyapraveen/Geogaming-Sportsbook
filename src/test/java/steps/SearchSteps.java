package steps;

import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import pages.HomePage;
import pages.ResultsPage;
import support.DriverManager;

public class SearchSteps {

    HomePage home;
    ResultsPage results;

    @Given("I open the Sportingbull homepage")
    public void open_homepage() {
        home = new HomePage(DriverManager.getDriver());
        home.open("https://en.sportingbull.com/sportsbook");
    }

    @When("I search for {string}")
    public void search_for(String query) {
        home.focusSearch(15);
        home.typeQuery(query, 10);
        home.submitEnter(5);
        results = new ResultsPage(DriverManager.getDriver(), 30);
        results.waitLoaded();
    }

    @Then("I should see search results displayed")
    public void i_should_see_search_results_displayed() {
        Assertions.assertTrue(results.hasResultsRowsVisible(),
                "Expected search results but found none.");
    }

    /* ---- No Results / Error ---- */
    @Then("I should see a friendly no-results message")
    public void i_should_see_a_friendly_no_results_message() {
        Assertions.assertTrue(results.waitNoResultsMessage(30),
                "Expected a friendly 'no results' message, but it did not appear.");
    }

    /* ---- Clear Search ---- */
    @When("I type {string} in the search bar")
    public void i_type_in_the_search_bar(String text) {
        home.focusSearch(15);
        home.typeQuery(text, 10);
        results = new ResultsPage(DriverManager.getDriver(), 20);
        results.waitLoaded();
    }

    @When("I click the clear X in the search bar")
    public void i_click_the_clear_x_in_the_search_bar() {
        results.clickClearX();
    }

    @Then("the search input should be empty")
    public void the_search_input_should_be_empty() {
        Assertions.assertEquals("", results.getSearchBoxValue(), "Search input was not cleared.");
    }

    @Then("the search results should disappear")
    public void the_search_results_should_disappear() {
        // Primary signal: the overlay shows “Search History is empty”
        Assertions.assertTrue(results.waitHistoryEmpty(15),
                "Expected 'Search History is empty' after clearing.");
        // Secondary: no visible rows remain
        Assertions.assertFalse(results.hasResultsRowsVisible(),
                "Expected all search results to disappear after clearing.");
    }
}
