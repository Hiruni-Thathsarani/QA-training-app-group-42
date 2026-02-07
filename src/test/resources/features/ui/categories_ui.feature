Feature: Categories UI Tests

  @user
  Scenario: Categories list is read-only for user
    Given user is logged in as normal user
    When user opens categories page directly
    Then categories list should be visible
    And category actions should be read-only

  @user
  Scenario: Search categories by term
    Given user is logged in as normal user
    And categories exist
    When user opens categories page
    And user enters a category search term
    And user applies category search
    Then categories list should be filtered to matching results
    And no category errors should be shown

  @user
  Scenario: Filter categories by parent
    Given user is logged in as normal user
    And parent categories exist
    When user opens categories page
    And user selects a parent category filter
    Then categories list should show only selected parent categories

  @user
  Scenario: Navigate categories pagination
    Given user is logged in as normal user
    And more than one page of categories exist
    When user opens categories page
    And user navigates to next categories page
    Then next categories page should load with correct items

  @debug_filter_plants
  @user
  Scenario: User can filter plants by category
    Given user is logged in as normal user
    When user opens plants page
    And user selects a plant category filter
    Then plants list should show only selected category

  @admin
  Scenario: Admin can add a valid main category
    Given user is logged in as admin
    When admin opens categories page
    And admin adds a main category
    Then category should appear in list

  @admin
  Scenario: Admin can edit and update a category name
    Given user is logged in as admin
    When admin opens categories page
    And admin edits a category name
    Then category update should be visible

  @admin
  Scenario: Admin can delete a category that has no dependencies
    Given user is logged in as admin
    When admin opens categories page
    And admin deletes a category with no dependencies
    Then category should be removed from list

  @admin
  Scenario: Admin can add a Plant under a sub-category
    Given user is logged in as admin
    When admin opens plants page
    And admin adds a plant under a sub-category
    Then plant should appear in list

  @admin
  Scenario: Admin can view sales list and see Sell Plant action
    Given user is logged in as admin
    When admin opens sales page
    Then sales list should be visible
    And sell plant action should be visible
