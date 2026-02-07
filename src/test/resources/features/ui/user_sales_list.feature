Feature: User Sales List UI Tests

  # ===== NEW CODE - USER SALES LIST TESTS START =====
  Scenario: User can view Sales list in read-only mode
    Given user is logged in as normal user
    When user opens sales page
    Then sales page should be visible
    And sales should be read only for user

  Scenario: Pagination works on Sales list for User
    Given user is logged in as normal user
    And more than one page of sales exist
    When user opens sales page
    And user navigates to next sales page
    Then next sales page should load with correct items

  Scenario: Sales list default ordering by Sold Date
    Given user is logged in as normal user
    And multiple sales records with different sold dates exist
    When user opens sales page
    Then sales list should be sorted by sold date descending
  # ===== NEW CODE - USER SALES LIST TESTS END =====
