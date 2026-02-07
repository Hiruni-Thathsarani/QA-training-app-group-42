Feature: Role Pages UI Tests

  Scenario: TC-004 User UI - Categories list read-only
    Given user is logged in as normal user
    When user opens categories page
    Then categories page should be visible
    And categories should be read only for user

  Scenario: TC-005 User UI - Plants list read-only
    Given user is logged in as normal user
    When user opens plants page
    Then plants page should be visible
    And plants should be read only for user
