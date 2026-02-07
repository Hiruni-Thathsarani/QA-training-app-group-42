Feature: User Sales API Tests

  Scenario: TC-046 Verify User can fetch paginated sales via API
    Given authenticated user token is available for sales api tests
    When user requests paginated sales with page 0 and size 10
    Then sales api response status should be 200
    And paginated sales data should be present in response
    And sales pagination metadata should be present

  Scenario: TC-047 Verify User can fetch sale by ID via API
    Given authenticated user token is available for sales api tests
    And at least one sale exists for user sales api tests
    When user requests sale by existing id via sales api
    Then sales api response status should be 200
    And sale details should be present in response
    And returned sale id should match requested sale id
