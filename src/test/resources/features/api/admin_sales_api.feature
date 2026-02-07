Feature: Admin Sales API Tests

  # // ===== NEW CODE - ADMIN SALES API TESTS START =====
  Scenario: TC-056 Verify Admin can create sale via API and stock is reduced
    Given authenticated admin token is available for admin sales api tests
    And at least one sellable plant exists for admin sales api tests
    And current stock is captured for selected plant in admin sales api tests
    When admin creates sale for selected plant with quantity 1 via sales api
    Then admin sales api response status should allow created sale
    And selected plant stock should be reduced by exactly 1 in admin sales api tests

  Scenario: TC-057 Verify API blocks overselling
    Given authenticated admin token is available for admin sales api tests
    And at least one low stock plant exists for admin sales api tests
    And current stock is captured for selected plant in admin sales api tests
    When admin attempts to oversell selected plant via sales api
    Then admin sales api response status should indicate oversell is blocked
    And oversell response should indicate insufficient stock for admin sales api tests
    And selected plant stock should remain unchanged after oversell attempt in admin sales api tests
  # // ===== NEW CODE - ADMIN SALES API TESTS END =====
