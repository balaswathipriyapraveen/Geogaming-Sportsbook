Feature: Basic Search

  Background:
    Given I open the Sportingbull homepage

  Scenario: Search returns results for a known team
    When I search for "Arsenal"
    Then I should see search results displayed

  Scenario: No Results / Error
    When I search for "zzzxx"
    Then I should see a friendly no-results message

  Scenario: Clear Search
    When I type "Arsenal" in the search bar
    And I click the clear X in the search bar
    Then the search input should be empty
    And the search results should disappear

