Feature: Maria's Padel Court Booking Journey
  As Maria, a renter
  I want to search and book padel courts
  So that I can play padel on Thursday evening

  Background:
    Given the SportsLink application is running

  Scenario: Maria searches for padel courts in Aveiro
    Given Maria is on the search page
    When she searches for "Padel" courts in "Aveiro" for "19:00-21:00"
    Then she should see 3 available facilities
    And the results should include "Padel Club Aveiro"
    And the results should include "Sports Center"
    And the results should include "Academy Pro"

  Scenario: Maria views facility details
    Given Maria searches for padel courts
    When she clicks on "Padel Club Aveiro"
    Then she should see the facility details
    And she should see the equipment list
    And she should see available time slots

  Scenario: Maria books a padel court
    Given Maria has selected "Padel Club Aveiro"
    And the time slot "19:00-21:00" is available
    When she books the court for "2025-11-27" at "19:00-21:00"
    Then the booking should be confirmed
    And the booking status should be "CONFIRMED"
    And she should receive a booking confirmation

  Scenario: Maria checks equipment availability
    Given Maria is viewing "Padel Club Aveiro"
    When she checks the equipment list
    Then she should see "Bola" available
    And she should see "Raquete" available
    And she should see "Rede" available

  Scenario: Maria filters search results
    Given Maria is on the search page
    When she filters by sport "Padel"
    And she filters by location "Aveiro"
    And she filters by time "19:00"
    Then she should only see facilities matching all criteria
    And all results should be in "Aveiro"
    And all results should offer "Padel"

  Scenario: Maria cancels a booking
    Given Maria has a confirmed booking with id 1
    When she cancels the booking
    Then the booking status should be "CANCELLED"
    And the time slot should become available again

  Scenario: Maria updates booking time
    Given Maria has a confirmed booking with id 1
    When she changes the time to "20:00-22:00"
    Then the booking should be updated
    And the new time should be "20:00-22:00"
    And the booking status should be "UPDATED"
