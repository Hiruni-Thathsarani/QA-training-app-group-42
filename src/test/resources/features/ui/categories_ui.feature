Feature: Categories UI Tests

  Scenario: Open inventory from dashboard as user
    Given user is logged in as normal user
    When user opens dashboard
    And user clicks open inventory
    Then inventory view should be visible

  Scenario: Categories list is read-only for user
    Given user is logged in as normal user
    When user opens categories page directly
    Then categories list should be visible
    And category actions should be read-only

  Scenario: Search categories by term
    Given user is logged in as normal user
    And categories exist
    When user opens categories page
    And user enters a category search term
    And user applies category search
    Then categories list should be filtered to matching results
    And no category errors should be shown

  Scenario: Filter categories by parent
    Given user is logged in as normal user
    And parent categories exist
    When user opens categories page
    And user selects a parent category filter
    Then categories list should show only selected parent categories

  Scenario: Navigate categories pagination
    Given user is logged in as normal user
    And more than one page of categories exist
    When user opens categories page
    And user navigates to next categories page
    Then next categories page should load with correct items
