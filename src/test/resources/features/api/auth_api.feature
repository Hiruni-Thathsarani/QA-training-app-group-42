Feature: Authentication API Tests (Member 1)

  Scenario: Admin login API success
    Given admin logs in via API
    Then token should be available

  Scenario: User login API success
    Given user logs in via API
    Then token should be available

  Scenario: Invalid login should fail
    When login via API with "wrong" and "wrong123"
    Then api response status should be 401

  Scenario: Admin token should allow admin endpoint
    Given admin logs in via API
    When admin creates category via API
    Then api response status should be 200

  Scenario: User token should be forbidden for admin endpoint
    Given user logs in via API
    When user tries to create category via API
    Then api response status should be 403

  Scenario: Missing token should be unauthorized
    When request create category without token
    Then api response status should be 401

  Scenario: Invalid token should be unauthorized
    When request create category with invalid token
    Then api response status should be 401

  Scenario: Admin login wrong password
    When login via API with "admin" and "wrongpass"
    Then api response status should be 401

  Scenario: User login wrong password
    When login via API with "user" and "wrongpass"
    Then api response status should be 401

  Scenario: Empty login payload should fail
    When login via API with "" and ""
    Then api response status should be 400
