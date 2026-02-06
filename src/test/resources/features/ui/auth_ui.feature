Feature: Authentication UI Tests (Member 1)

  Scenario: Admin login success
    Given user is on login page
    When user logs in as admin
    Then dashboard should be visible

  Scenario: Admin login invalid credentials
    Given user is on login page
    When user logs in with invalid credentials
    Then error message should be shown

  Scenario: Admin logout success
    Given user is logged in as admin
    When user logs out
    Then login page should be visible

  Scenario: Access dashboard without login should redirect to login
    Given user is not logged in
    When user opens dashboard directly
    Then user should be redirected to login

  Scenario: User login success
    Given user is on login page
    When user logs in as normal user
    Then dashboard should be visible

  Scenario: User login invalid credentials
    Given user is on login page
    When user logs in with invalid credentials
    Then error message should be shown

  Scenario: User logout success
    Given user is logged in as normal user
    When user logs out
    Then login page should be visible

  Scenario: User should not see admin actions
    Given user is logged in as normal user
    Then admin actions should not be visible

  Scenario: User cannot open admin-only sell page directly
    Given user is logged in as normal user
    When user opens sell page directly
    Then access should be blocked

  Scenario: Invalid session access to protected page
    Given user is not logged in
    When user opens sell page directly without login
    Then user should be redirected to login
