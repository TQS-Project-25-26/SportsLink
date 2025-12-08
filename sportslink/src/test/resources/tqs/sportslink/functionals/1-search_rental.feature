Feature: Functional testing for facility search and booking

  # ============================================================
  # SEARCH SCENARIOS
  # User Story: SL-27 (Search by name, sport type)
  # Epic: SL-15 (Field Discovery & Catalog - se existir)
  # ============================================================
  
  @SL-27 @SL-104 @search @functional
  Scenario: User searches for fields using the sport dropdown
    Given I am on the main search page
    When I select the sport "Football" from the dropdown
    And I press the search button
    Then I should see facilities related to "Football"

  @SL-27 @SL-104 @search @functional
  Scenario: User searches for padel courts in Aveiro
    Given I am on the main search page
    When I select the sport "Padel" from the dropdown
    And I enter "Aveiro" into the location field
    And I press the search button
    Then I should see facilities located in "Aveiro"

  # ============================================================
  # FACILITY DETAILS
  # User Story: SL-24 (View detailed facility informations)
  # Epic: SL-15 (Field Discovery & Catalog - se existir)
  # ============================================================

  @SL-24 @SL-27 @SL-104 @details @functional
  Scenario: User opens the details of the first search result
    Given I performed a search for "Padel" in "Aveiro"
    When I click on the first facility result
    Then I should be on the facility details page

  @SL-24 @SL-32 @SL-104 @details @equipment @functional
  Scenario: User opens equipment list from facility details
    Given I am on the facility details page for facility 1
    When I click the button to view all equipment
    Then I should see at least one equipment card

  # ============================================================
  # TIME SLOT SELECTION & BOOKING
  # User Story: SL-29 (Time slot selection and reservation)
  # User Story: SL-26 (Book slot)
  # Epic: SL-20 (Booking & Reservation System)
  # ============================================================

  @SL-29 @SL-32 @SL-20 @SL-104 @booking @equipment @functional
  Scenario: User selects available equipment and proceeds to booking
    Given I am viewing equipment for facility 1
    When I select at least one available equipment item
    And I press the continue button
    Then I should be on the booking page

  @SL-29 @SL-20 @SL-104 @booking @functional
  Scenario: User completes a booking
    Given I am on the booking page for facility 1
    When I fill the booking form with time "18:00" and duration "2" hours
    And I confirm the booking
    Then a booking confirmation modal should appear with an ID

  # ============================================================
  # END-TO-END FLOW
  # Multiple User Stories: SL-27 (Search) + SL-24 (Details) + 
  #                        SL-29 (Time slot) + SL-32 (Equipment)
  # Epic: SL-20 (Booking & Reservation System)
  # ============================================================

  @SL-27 @SL-24 @SL-29 @SL-32 @SL-20 @SL-104 @e2e @critical @functional
  Scenario: Complete booking flow from search to confirmation
    Given I am on the main search page
    # Search phase (SL-27)
    When I select the sport "Padel" from the dropdown
    And I enter "Aveiro" into the location field
    And I press the search button
    Then I should see facilities located in "Aveiro"
    # Details phase (SL-24)
    When I click on the first facility result
    Then I should be on the facility details page
    # Equipment selection phase (SL-32)
    When I click the button to view all equipment
    Then I should see at least one equipment card
    When I select at least one available equipment item
    And I press the continue button
    Then I should be on the booking page
    # Booking phase (SL-29)
    When I fill the booking form with time "20:00" and duration "2" hours
    And I confirm the booking
    Then a booking confirmation modal should appear with an ID
    And the booking should be stored in the system