Feature: User Authentication

  @SL-10 @auth @functional
  Scenario: User registers specifically
    Given I am on the registration page
    When I fill the registration form with distinct email "newuser@example.com" and password "password123"
    And I submit the registration form
    Then I should be redirected to the main page
