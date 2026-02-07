@ui @categories @215114F @user
Feature: Categories User UI Tests (TC-081 to TC-085)
  As a normal user
  I want to view categories with pagination, sorting and filtering
  So that I can browse categories effectively

  Background:
    Given user is logged in as normal user
    And user navigates to categories page

  @TC-081
  Scenario: TC-081 User UI Categories pagination
    Given categories exist with more than one page
    When user clicks Next page button
    Then the category list should change to next page

  @TC-082
  Scenario: TC-082 User UI Categories sorting by Name
    When user clicks on Name column header to sort
    Then categories should be sorted by Name

  @TC-082
  Scenario: TC-082 User UI Categories sorting by ID
    When user clicks on ID column header to sort
    Then categories should be sorted by ID

  @TC-083
  Scenario: TC-083 User UI Filter by parent category
    Given parent categories exist
    When user selects a parent category from filter dropdown
    Then only sub-categories of selected parent should be displayed

  @TC-084
  Scenario: TC-084 User UI Admin actions restricted for user
    Then Add Category button should not be visible for user
    And Edit buttons should not be visible for user
    And Delete buttons should not be visible for user

  @TC-085
  Scenario: TC-085 User UI Empty state when no category found
    When user searches for a non-existent category "ZZZZNONEXISTENT999"
    Then empty state message should be displayed
