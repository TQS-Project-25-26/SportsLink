Feature: Field Creation

  @SL-33 @owner @field @functional
  Scenario: Owner can open the add facility modal
    Given I am logged in as an owner
    And I am on the owner dashboard
    When I click the add facility button
    Then I should see the facility form
