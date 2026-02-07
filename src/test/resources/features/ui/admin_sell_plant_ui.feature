Feature: Admin Sell Plant UI Tests

  # ===== NEW CODE - USER SALES LIST TESTS START =====
  Scenario: Verify Admin can access Sell Plant page
    Given user is logged in as admin
    When admin opens sell plant page directly
    Then sell plant form should be visible for admin
    And sell plant required fields should be visible
    And no sell plant access errors should be shown

  Scenario: Verify quantity field validation on Sell Plant form
    Given user is logged in as admin
    And sell plant form is open for admin
    When admin submits sell form without quantity
    Then quantity validation message should be shown
    And sale should not be created from sell plant form
    And admin should remain on sell plant form

  Scenario: Verify overselling is blocked in UI
    Given user is logged in as admin
    And sell plant form is open for admin
    And limited stock plant is available for selling
    When admin submits quantity greater than available stock
    Then insufficient stock message should be shown
    And sale should not be created from sell plant form
    And no redirect to sales list should occur
  # ===== NEW CODE - USER SALES LIST TESTS END =====
