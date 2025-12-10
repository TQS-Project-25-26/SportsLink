Feature: User Login

  @SL-42 @login @functional
  Scenario: User logs in successfully
    Given I am on the login page
    When I fill the login form with "test@sportslink.com" and "password123"
    And I press the login button
    Then I should be redirected to the main page
