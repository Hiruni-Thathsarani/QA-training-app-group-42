Feature: Authentication UI Tests

  # ---------- USER UI ----------
  Scenario: TC-001 User UI - Login success (User)
    Given user is on login page
    When user logs in as normal user
    Then dashboard should be visible

  Scenario: TC-002 User UI - Login invalid password (User)
    Given user is on login page
    When user logs in with invalid credentials
    Then error message should be shown

  Scenario: TC-003 User UI - Dashboard loads (User)
    Given user is logged in as normal user
    Then dashboard should be visible

  # ---------- ADMIN UI ----------
  Scenario: TC-011 Admin UI - Login success (Admin)
    Given user is on login page
    When user logs in as admin
    Then dashboard should be visible

  Scenario: TC-012 Admin UI - Login invalid password (Admin)
    Given user is on login page
    When user logs in with invalid credentials
    Then error message should be shown

  Scenario: TC-013 Admin UI - Dashboard loads (Admin)
    Given user is logged in as admin
    Then dashboard should be visible
