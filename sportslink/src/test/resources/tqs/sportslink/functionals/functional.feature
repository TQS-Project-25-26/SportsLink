Feature: Functional testing for facility search and booking

  # -----------------------------------------------------------
  # Scenario 1 - Search by sport dropdown
  # -----------------------------------------------------------
  Scenario: User searches for fields using the sport dropdown
    Given I am on the main search page
    When I select the sport "Football" from the dropdown
    And I press the search button
    Then I should see facilities related to "Football"

  # -----------------------------------------------------------
  # Scenario 2 - Search by sport + location text input
  # -----------------------------------------------------------
  Scenario: User searches for padel courts in Aveiro
    Given I am on the main search page
    When I select the sport "Padel" from the dropdown
    And I enter "Aveiro" into the location field
    And I press the search button
    Then I should see facilities located in "Aveiro"

  # -----------------------------------------------------------
  # Scenario 3 - User opens the details page of a facility
  # -----------------------------------------------------------
  Scenario: User opens the details of the first search result
    Given I performed a search for "Padel" in "Aveiro"
    When I click on the first facility result
    Then I should be on the facility details page

  # -----------------------------------------------------------
  # Scenario 4 - User views equipment list of a facility
  # -----------------------------------------------------------
  Scenario: User opens equipment list from facility details
    Given I am on the facility details page for facility 1
    When I click the button to view all equipment
    Then I should see at least one equipment card

  # -----------------------------------------------------------
  # Scenario 5 - User selects equipment and proceeds to booking
  # -----------------------------------------------------------
  Scenario: User selects available equipment and proceeds to booking
    Given I am viewing equipment for facility 1
    When I select at least one available equipment item
    And I press the continue button
    Then I should be on the booking page

  # -----------------------------------------------------------
  # Scenario 6 - User completes booking successfully
  # -----------------------------------------------------------
  Scenario: User completes a booking
    Given I am on the booking page for facility 1
    When I fill the booking form with valid data
    And I confirm the booking
    Then a booking confirmation modal should appear with an ID
