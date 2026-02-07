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

  # ===== NEW CODE - ADMIN SALES UI TESTS START =====
  Scenario: Verify stock is reduced after successful sale (Admin UI)
    Given user is logged in as admin
    And admin has a sellable plant in plants list
    When admin sells one unit of the selected plant
    And admin navigates back to plants list
    Then sale should be created successfully for the selected plant
    And selected plant stock should be reduced by exactly one
    And updated stock should be reflected for the selected plant in plants list

  Scenario: Verify delete sale requires confirmation (Admin UI)
    Given user is logged in as admin
    And at least one sale exists for admin
    When admin clicks delete on a sale record
    Then delete confirmation dialog should be displayed for admin
    And sale should not be deleted without admin confirmation
    And deletion should require explicit admin confirmation
  # ===== NEW CODE - ADMIN SALES UI TESTS END =====
