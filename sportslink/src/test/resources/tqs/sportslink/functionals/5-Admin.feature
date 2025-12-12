@REQ_SL-343
Feature: Admin Management
  
  # ============================================================
  # ADMIN ACCESS & DASHBOARD
  # Epic: SL-23 (Admin Page)
  # ============================================================

  @admin @functional
  Scenario: Admin logs in and sees dashboard
    Given I exist as an admin with email "admin@admin.com" and password "pwdAdmin"
    When I login as "admin@admin.com" with password "pwdAdmin"
    Then I should be redirected to the admin dashboard
    And I should see system statistics

  @admin @functional
  Scenario: Admin views user management
    Given I am logged in as admin
    And a user "owner@sportslink.com" exists and is active
    When I navigate to the users management page
    Then I should see a list of users
    And I should see user "owner@sportslink.com"

  @admin @functional
  Scenario: Admin blocks a user
    Given I am logged in as admin
    And a user "test@sportslink.com" exists and is active
    When I navigate to the users management page
    And I click to block user "test@sportslink.com"
    Then the user "test@sportslink.com" should be marked as inactive

  @admin @functional
  Scenario: Admin Views Facilities
    Given I am logged in as admin
    And a facility "Campo de Futebol da Universidade de Aveiro" exists
    When I navigate to the facilities management page
    Then I should see a list of facilities
    And I should see facility "Campo de Futebol da Universidade de Aveiro"

  @admin @functional
  Scenario: Admin Deletes a Facility
    Given I am logged in as admin
    And a facility "To Be Deleted Facility" exists
    When I navigate to the facilities management page
    And I click to delete facility "To Be Deleted Facility"
    Then the facility "To Be Deleted Facility" should no longer appear in the list


