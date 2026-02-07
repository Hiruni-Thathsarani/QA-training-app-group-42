Feature: Admin Pages UI Tests

  Scenario: TC-014 Admin UI - Add Category page accessible
    Given user is logged in as admin
    When admin opens add category page
    Then add category page should be visible

  Scenario: TC-015 Admin UI - Sales page accessible
    Given user is logged in as admin
    When admin opens sales page
    Then sales list should be visible
