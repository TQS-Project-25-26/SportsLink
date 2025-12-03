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
  # User Story: SL-26 (Book slot) - se ainda existir
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
    When I fill the booking form with valid data
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
    When I fill the booking form with valid data
    And I confirm the booking
    Then a booking confirmation modal should appear with an ID
    And the booking should be stored in the system

  # ============================================================
  # ADDITIONAL SCENARIOS
  # Testing calendar view and availability
  # ============================================================

  @SL-28 @SL-20 @SL-104 @calendar @functional
  Scenario: User views facility availability in calendar
    Given I am on the facility details page for facility 1
    When I click the "View Calendar" button
    Then I should see a calendar view
    And I should see available time slots highlighted
    And I should see booked time slots marked as unavailable

  @SL-29 @SL-28 @SL-20 @SL-104 @booking @calendar @functional
  Scenario: User selects time slot from calendar
    Given I am viewing the calendar for facility 1
    When I click on an available time slot for "2025-12-15" at "14:00"
    Then the time slot should be selected
    And I should see booking details for that time slot
    When I confirm the time slot selection
    Then I should proceed to the booking form

  # ============================================================
  # NEGATIVE SCENARIOS
  # Testing error handling and validation
  # ============================================================

  @SL-29 @SL-32 @SL-20 @SL-104 @booking @equipment @negative @functional
  Scenario: User cannot proceed to booking without selecting equipment
    Given I am on the facility details page for facility 1
    When I click the button to view all equipment
    And I press the continue button without selecting equipment
    Then I should see an error message "Please select at least one equipment"
    And I should remain on the equipment selection view

  @SL-27 @SL-104 @search @negative @functional
  Scenario: User searches for a sport with no results
    Given I am on the main search page
    When I select the sport "Ice Hockey" from the dropdown
    And I press the search button
    Then I should see a message "No facilities found for this sport"
    And I should see a suggestion to try different search criteria

  @SL-29 @SL-20 @SL-104 @booking @negative @functional
  Scenario: User cannot book with invalid contact information
    Given I am on the booking page for facility 1
    When I fill the booking form with invalid email "notanemail"
    And I confirm the booking
    Then I should see a validation error for the email field
    And the booking should not be created

  @SL-29 @SL-28 @SL-20 @SL-104 @booking @calendar @negative @functional
  Scenario: User cannot select already booked time slot
    Given I am viewing the calendar for facility 1
    When I try to click on a time slot that is already booked
    Then the time slot should not be selectable
    And I should see a tooltip "This time slot is already booked"

  # ============================================================
  # BOOKING MANAGEMENT SCENARIOS
  # User Story: SL-30 (Booking management - view, modify, cancel)
  # Epic: SL-20
  # ============================================================

  @SL-30 @SL-20 @SL-104 @booking-management @functional
  Scenario: User views their booking details
    Given I have an active booking with ID "12345"
    When I navigate to "My Bookings"
    And I click on booking "12345"
    Then I should see complete booking details including:
      | Field Name    | Campo Municipal Aveiro |
      | Date          | 2025-12-15            |
      | Time          | 14:00 - 16:00         |
      | Status        | CONFIRMED             |
      | Payment Status| PAID                  |

  @SL-30 @SL-20 @SL-104 @booking-management @functional
  Scenario: User cancels their booking
    Given I have an active booking with ID "12345"
    When I navigate to "My Bookings"
    And I click on booking "12345"
    And I click the "Cancel Booking" button
    And I confirm the cancellation
    Then the booking status should change to "CANCELLED"
    And I should receive a cancellation confirmation

  @SL-30 @SL-29 @SL-20 @SL-104 @booking-management @functional
  Scenario: User modifies their booking time slot
    Given I have an active booking with ID "12345" for "2025-12-15" at "14:00"
    When I navigate to "My Bookings"
    And I click on booking "12345"
    And I click the "Modify Booking" button
    And I select a new time slot for "2025-12-15" at "16:00"
    And I confirm the modification
    Then the booking should be updated with the new time
    And I should receive a modification confirmation