@api @user
Feature: User API Permissions and Read Access

  Scenario: User can fetch paginated plants (200)
    Given a valid user API token
    When the user requests paged plants with page 0 and size 5
    Then the response status should be 200
    And the response should contain a paged list of plants

  Scenario: User can retrieve categories summary (200)
    Given a valid user API token
    When the user requests the categories summary
    Then the response status should be 200
    And the summary data should be returned

  Scenario: User cannot create a plant via API (403)
    Given a valid user API token
    When the user attempts to create a plant with a valid payload
    Then the response status should be 403

  Scenario: User can fetch plants by category API
    Given a valid user API token
    And an existing category id is available
    When the user requests plants by category
    Then the response status should be 200
    And the response should contain a list of plants

  Scenario: User can get plants summary
    Given a valid user API token
    When the user requests the plants summary
    Then the response status should be 200
    And the plants summary should be returned

  @api @admin
  Scenario: Admin can fetch sales list (200)
    Given a valid admin API token
    When the admin requests the sales list
    Then the response status should be 200
    And the response should contain a sales list

  @api @admin
  Scenario: Admin can delete an existing sale (204/200)
    Given a valid admin API token
    And an existing sale id is available for admin
    When the admin deletes the sale by id
    Then the response status should be one of 200 or 204
    And the deleted sale should not be retrievable

  @api @admin
  Scenario: Admin can update plant details (200)
    Given a valid admin API token
    And an existing plant id is available for admin
    When the admin updates the plant details
    Then the response status should be 200
    And the plant details should be updated

  @api @admin
  Scenario: Admin cannot create plant with invalid price
    Given a valid admin API token
    And a valid sub-category id is available
    When the admin creates a plant with invalid price
    Then the response status should be one of 400 or 422
    And the invalid-price plant should not be created

  @api @admin
  Scenario: Admin cannot sell plant when quantity exceeds stock
    Given a valid admin API token
    And a plant with low stock is available
    When the admin attempts to sell more than available stock
    Then the response status should be one of 400 or 409
    And the plant stock should remain unchanged
