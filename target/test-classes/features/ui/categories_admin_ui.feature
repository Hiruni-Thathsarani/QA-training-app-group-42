@ui @categories @215114F @admin
Feature: Categories Admin UI Tests (TC-091 to TC-095)
  As an admin user
  I want to manage categories with proper validation
  So that data integrity is maintained

  Background:
    Given user is logged in as admin
    And user navigates to categories page

  @TC-091
  Scenario: TC-091 Admin UI Category name required validation
    When admin clicks Add Category button
    And admin leaves category name blank
    And admin clicks Save button
    Then validation error should be displayed for required name

  @TC-092
  Scenario: TC-092 Admin UI Category name length validation
    When admin clicks Add Category button
    And admin enters category name with invalid length "VERYLONGCATEGORYNAME"
    And admin clicks Save button
    Then validation error should be displayed for name length

  @TC-093
  Scenario: TC-093 Admin UI Create sub-category
    Given a main category exists
    When admin clicks Add Category button
    And admin enters valid category name
    And admin selects a parent category
    And admin clicks Save button
    Then sub-category should be created successfully
    And success message should be displayed

  @TC-094
  Scenario: TC-094 Admin UI Edit category cancel
    Given categories exist
    When admin clicks Edit on a category
    And admin modifies the category name
    And admin clicks Cancel button
    Then user should be navigated back to categories list
    And category should not be updated

  @TC-095
  Scenario: TC-095 Admin UI Delete parent category blocked
    Given a parent category with children exists
    When admin attempts to delete the parent category
    Then delete should be blocked
    And error message about children should be displayed
