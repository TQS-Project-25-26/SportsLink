Feature: Owner Suggestions

  @REQ_SL-32 @owner @suggestions @functional
  Scenario: Owner views intelligent suggestions
    Given I am logged in as an owner
    And I am on the owner dashboard
    When I click the View Suggestions button
    Then I should be on the suggestions page
    And I should see a list of suggested activities
