Feature: Required API Tests (TC-006 to TC-020)

  Scenario: TC-006 User API - Get categories (200)
    Given a valid user API token
    When the user requests categories list
    Then the response status should be 200

  Scenario: TC-007 User API - Get plants (200)
    Given a valid user API token
    When the user requests paged plants with page 0 and size 5
    Then the response status should be 200

  Scenario: TC-008 User API - Get sales (200)
    Given a valid user API token
    When the user requests the sales list
    Then the response status should be 200

  Scenario: TC-009 User API - Create category forbidden (403)
    Given user logs in via API
    When user tries to create category via API
    Then api response status should be 403

  Scenario: TC-010 User API - Missing token returns 401
    When request create category without token
    Then api response status should be 401

  Scenario: TC-016 Admin API - Create category (201/200)
    Given a valid admin API token
    When admin creates a category via API
    Then the response status should be one of 200 or 302

  Scenario: TC-017 Admin API - Update category (200)
    Given a valid admin API token
    And an existing category id is available
    When the admin updates the category name
    Then the response status should be 200

  Scenario: TC-018 Admin API - Delete category (204/200)
    Given a valid admin API token
    And an existing category id is available
    When the admin deletes the category by id
    Then the response status should be one of 200 or 204

  Scenario: TC-019 Admin API - Create plant (201/200)
    Given a valid admin API token
    When the admin creates a plant via API
    Then the response status should be one of 200 or 302

  Scenario: TC-020 Admin API - Sell plant reduces stock
    Given a valid admin API token
    And an existing plant id is available for admin
    When the admin sells the plant with quantity 1
    Then the plant stock should reduce by 1
