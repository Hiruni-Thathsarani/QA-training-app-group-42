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

  Scenario: User cannot create a sale (sell plant) via API (403)
    Given a valid user API token
    And an existing plant id is available
    When the user attempts to sell the plant with quantity 1
    Then the response status should be 403

  Scenario: User cannot delete a sale via API (403)
    Given a valid user API token
    And an existing sale id is available
    When the user attempts to delete the sale
    Then the response status should be 403
