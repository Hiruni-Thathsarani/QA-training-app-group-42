@api @categories @215114F @user
Feature: Categories User API Tests (TC-086 to TC-090)
  As a normal user accessing the API
  I want to view categories but not modify them
  So that read-only access is enforced

  Background:
    Given user logs in via API

  @TC-086
  Scenario: TC-086 User API Categories page with pagination
    When user requests GET /api/categories/page with page=0 and size=5
    Then API response status should be 200
    And response should contain paged category data

  @TC-087
  Scenario: TC-087 User API Main categories
    When user requests GET /api/categories/main
    Then API response status should be 200
    And response should contain list of main categories

  @TC-088
  Scenario: TC-088 User API Sub-categories
    When user requests GET /api/categories/sub-categories
    Then API response status should be 200
    And response should contain list of sub-categories

  @TC-089
  Scenario: TC-089 User API Create category forbidden
    When user sends POST /api/categories with valid payload
    Then API response status should be 403 Forbidden

  @TC-090
  Scenario: TC-090 User API Update category forbidden
    Given a category exists via admin API
    When user sends PUT /api/categories/{id} with valid payload
    Then API response status should be 403 Forbidden
