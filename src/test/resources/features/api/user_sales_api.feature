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

  # // ===== NEW CODE - USER SALES API TESTS START =====
  Scenario: TC-048 Verify User is forbidden from creating sale via API
    Given authenticated user token is available for sales api tests
    And at least one plant exists for user sales api tests
    And current total sales count is captured for user sales api tests
    When user tries to create sale for existing plant via sales api
    Then sales api response status should be 403
    And forbidden response should indicate permission denied
    And total sales count should remain unchanged for user sales api tests

  Scenario: TC-049 Verify User is forbidden from deleting sale via API
    Given authenticated user token is available for sales api tests
    And at least one sale exists for user sales api tests
    When user tries to delete sale by existing id via sales api
    Then sales api response status should be 403
    And forbidden response should indicate permission denied
    And sale should still exist after forbidden delete attempt
  # // ===== NEW CODE - USER SALES API TESTS END =====

  # // ===== NEW CODE - USER SALES API TESTS START =====
  Scenario: TC-050 Verify unauthorized request returns 401
    When user requests all sales without authentication token via sales api
    Then sales api response status should be 401
    And unauthorized response should indicate missing or invalid authentication
    And no sales data should be returned for unauthorized sales request
  # // ===== NEW CODE - USER SALES API TESTS END =====
